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
        // 调试日志：记录沙箱调用输入
        log.info("沙箱调用输入：code={}, lang={}, inputList={}", 
                execCoderequest.getCode(), 
                execCoderequest.getLang(), 
                execCoderequest.getInputList());

        String code = execCoderequest.getCode();
        String lang = execCoderequest.getLang();
        List<String> inputList = execCoderequest.getInputList();

        // 只处理Java语言
        if (!"java".equalsIgnoreCase(lang)) {
            // 非Java语言，返回错误响应
            ExecCodeResponse errorResponse = new ExecCodeResponse();
            errorResponse.setStatus(3); // 3表示不支持的语言
            errorResponse.setMessage("暂不支持该语言，仅支持Java");
            errorResponse.setOutputList(new ArrayList<>());
            errorResponse.setJudgeInfo(new JudgeInfo());
            log.warn("不支持的语言：{}", lang);
            return errorResponse;
        }

        //1.把用户的代码保存为文件
        File userCodefile = saveCodeToFile(code);
        log.info("保存代码到文件：{}", userCodefile.getAbsolutePath());

        //2.编译代码，得到class文件
        ExecuteMessage compileFileExecuteMessage = compileFile(userCodefile);
        log.info("编译结果：{}", compileFileExecuteMessage);
        System.out.println(compileFileExecuteMessage);

        // 检查编译是否成功
        if (compileFileExecuteMessage.getExitValue() != 0) {
            // 编译失败，返回错误响应
            ExecCodeResponse errorResponse = new ExecCodeResponse();
            errorResponse.setStatus(3); // 3表示编译失败
            errorResponse.setMessage(compileFileExecuteMessage.getErrorMessage());
            errorResponse.setOutputList(new ArrayList<>());
            errorResponse.setJudgeInfo(new JudgeInfo());
            //5.文件清理
            boolean b = deleteFile(userCodefile);
            if (!b) {
                log.error("deleteFile error, userCodeFilePath = {}", userCodefile.getAbsolutePath());
            }
            return errorResponse;
        }

        //3.执行代码，得到输出结果
        List<ExecuteMessage> executeMessagesList;
        try {
            executeMessagesList = runFile(userCodefile, inputList);
            log.info("执行结果列表：{}", executeMessagesList);
        } catch (Exception e) {
            log.error("执行失败：", e);
            // 执行失败，返回错误响应
            ExecCodeResponse errorResponse = new ExecCodeResponse();
            errorResponse.setStatus(3); // 3表示执行失败
            errorResponse.setMessage("执行错误：" + e.getMessage());
            errorResponse.setOutputList(new ArrayList<>());
            errorResponse.setJudgeInfo(new JudgeInfo());
            //5.文件清理
            boolean b = deleteFile(userCodefile);
            if (!b) {
                log.error("deleteFile error, userCodeFilePath = {}", userCodefile.getAbsolutePath());
            }
            return errorResponse;
        }

        //4.收集整理输出结果
        ExecCodeResponse outPutResponse = getOutPutResponse(executeMessagesList);
        // 调试日志：记录沙箱调用输出
        log.info("沙箱调用输出：{}", outPutResponse);

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
        
        // 修改用户代码，将公共类名称改为Main，确保与文件名一致
        String modifiedCode = code;
        if (modifiedCode.contains("public class")) {
            // 使用更可靠的方式替换公共类名
            modifiedCode = modifiedCode.replaceAll("(?m)^\\s*public class\\s+[\\w]+\\s*", "public class Main");
        }
        
        log.info("修改后的代码：\n{}", modifiedCode);
        
        File userCodeFile = FileUtil.writeString(modifiedCode, userCodePath, StandardCharsets.UTF_8);
        return userCodeFile;
    }

    //2.编译代码，得到class文件
    public ExecuteMessage compileFile(File userCodeFile) {
        String compileCmd = String.format("javac -encoding utf-8 \"%s\"", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            // 不再抛出异常，而是返回编译结果
            return executeMessage;
        } catch (Exception e) {
            // 将异常信息封装到ExecuteMessage中返回
            ExecuteMessage executeMessage = new ExecuteMessage();
            executeMessage.setExitValue(1);
            executeMessage.setErrorMessage("编译过程中发生异常：" + e.getMessage());
            return executeMessage;
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
