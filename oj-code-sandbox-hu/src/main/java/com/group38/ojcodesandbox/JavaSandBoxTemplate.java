package com.group38.ojcodesandbox;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;

import cn.hutool.core.util.StrUtil;

import com.group38.ojcodesandbox.model.ExecCodeRequest;
import com.group38.ojcodesandbox.model.ExecCodeResponse;
import com.group38.ojcodesandbox.model.ExecuteMessage;
import com.group38.ojcodesandbox.model.JudgeInfo;

import com.group38.ojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class JavaSandBoxTemplate implements Sandbox {
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    //超时时间
    private static final long TIME_OUT=5000L;

    @Override
    public ExecCodeResponse execCode(ExecCodeRequest execCoderequest) {

        String code = execCoderequest.getCode();
        String lang = execCoderequest.getLang();
        List<String> inputList = execCoderequest.getInputList();

        //1.把用户的代码保存为文件
        //2.编译代码，得到class文件
        //3.执行代码，得到输出结果
        //4.收集整理输出结果
        //5.文件清理
        //6.错误处理，提升程序健壮性

        //1.把用户的代码保存为文件
        File userCodefile = saveCodeToFile(code);

        //2.编译代码，得到class文件
        ExecuteMessage compileFileExecuteMessage = compileFile(userCodefile);
        System.out.println(compileFileExecuteMessage);

        //3.执行代码，得到输出结果
        List<ExecuteMessage> executeMessagesList = runFile(userCodefile, inputList);

        //4.收集整理输出结果
        ExecCodeResponse outPutResponse = getOutPutResponse(executeMessagesList);

        //5.文件清理
        boolean b = deleteFile(userCodefile);
        if (!b){
            log.error("deleteFile error, userCodeFilePath = {}", userCodefile.getAbsolutePath());
        }

        return outPutResponse;
    }

    //1.把用户的代码保存为文件
    public File saveCodeToFile(String code) {
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;

        //判断全局代码是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        //将用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        return userCodeFile;
    }

    //2.编译代码，得到class文件
    public ExecuteMessage compileFile(File userCodeFile) {
        String compileCmd = String.format("javac -encoding utf-8 \"%s\"", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            if (executeMessage.getExitValue() != 0){
                throw new RuntimeException("编译错误");
            }
            return executeMessage;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //3.执行代码，得到执行结果列表
    public List<ExecuteMessage> runFile(File userCodeFile , List<String> inputList){
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();

        List<ExecuteMessage> executeMessagesList = new ArrayList<>();
        for (String inputArgs : inputList) {
            StopWatch stopWatch = new StopWatch();
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp \"%s\" Main \"%s\"", userCodeParentPath, inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                //超时控制
                new Thread(()->{
                    try {
                        Thread.sleep(TIME_OUT);
                        //System.out.println("超时了，中断");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                System.out.println(executeMessage);
                executeMessagesList.add(executeMessage);
            } catch (Exception e) {
                throw new RuntimeException("执行错误", e);
            }
        }
        return executeMessagesList;
    }

    //4.收集整理输出结果
    public ExecCodeResponse getOutPutResponse(List<ExecuteMessage> executeMessagesList) {
        ExecCodeResponse execCodeResponse = new ExecCodeResponse();
        List<String> outputList = new ArrayList<>();
        //取用时最大值，用于判断是否超时
        long maxTime = 0;
        for (ExecuteMessage executeMessage : executeMessagesList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                execCodeResponse.setMessage(errorMessage);
                //用户提交代码中存在错误
                execCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
        }

        //正常运行完成
        if (outputList.size() == executeMessagesList.size()) {
            execCodeResponse.setStatus(1);
        }

        execCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        //获取内存占用未实现
//        judgeInfo.setMemory();

        execCodeResponse.setJudgeInfo(judgeInfo);

        return execCodeResponse;
    }

    //5.文件清理
    public boolean deleteFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
    }

    //6.错误处理，提升程序健壮性
    //获取错误响应
    private ExecCodeResponse getErrorResponse(Throwable e) {
        ExecCodeResponse execCodeResponse = new ExecCodeResponse();
        execCodeResponse.setMessage(e.getMessage());
        //表示代码沙箱错误
        execCodeResponse.setStatus(2);
        execCodeResponse.setJudgeInfo(new JudgeInfo());
        execCodeResponse.setOutputList(new ArrayList<>());
        return execCodeResponse;
    }

}
