package com.group38.ojcodesandbox;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.group38.ojcodesandbox.model.ExecCodeRequest;
import com.group38.ojcodesandbox.model.ExecCodeResponse;
import com.group38.ojcodesandbox.model.ExecuteMessage;
import com.group38.ojcodesandbox.model.JudgeInfo;
import com.group38.ojcodesandbox.security.DefaultSecurityManager;
import com.group38.ojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JavaNativeSandboxOld implements Sandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    //超时时间
    private static final long TIME_OUT=5000L;
    //黑名单
    private static final List<String> blackList = Arrays.asList("Files", "exec");

    public static final WordTree WORD_TREE;

    static{
        //初始化字典树
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(blackList);
    }

    //测试代码
    public static void main(String[] args) {
        JavaNativeSandboxOld javaNativeSandbox = new JavaNativeSandboxOld();
        ExecCodeRequest execCodeRequest = new ExecCodeRequest();
        execCodeRequest.setInputList(Arrays.asList("2", "3"));
//        String code = ResourceUtil.readStr("testCode/unsafeCode/MemoryError.java", StandardCharsets.UTF_8);
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
        execCodeRequest.setCode(code);
        execCodeRequest.setLang("java");
        ExecCodeResponse execCodeResponse = javaNativeSandbox.execCode(execCodeRequest);
        System.out.println(execCodeResponse);
    }


    @Override
    public ExecCodeResponse execCode(ExecCodeRequest execCoderequest) {
        //System.setSecurityManager(new DefaultSecurityManager());

        String code = execCoderequest.getCode();
        String lang = execCoderequest.getLang();
        List<String> inputList = execCoderequest.getInputList();

        //校验代码中是否包含黑名单中的命令
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if (foundWord != null){
            System.out.println("包含禁止词：" + foundWord.getFoundWord());
            return null;
        }

        //1.把用户的代码保存为文件
        //2.编译代码，得到class文件
        //3.执行代码，得到输出结果
        //4.收集整理输出结果
        //5.文件清理
        //6.错误处理，提升程序健壮性

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

        //2.编译代码，得到class文件
        String compileCmd = String.format("javac -encoding utf-8 \"%s\"", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(executeMessage);
        } catch (Exception e) {
            return getErrorResponse(e);
        }

        //3.执行代码，得到输出结果
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
                return getErrorResponse(e);
            }
        }

        //4.收集整理输出结果
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

        //5.文件清理
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
        }

        return execCodeResponse;
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
