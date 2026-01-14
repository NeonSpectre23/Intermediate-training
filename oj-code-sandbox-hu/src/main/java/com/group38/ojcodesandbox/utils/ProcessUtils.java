package com.group38.ojcodesandbox.utils;

import cn.hutool.core.date.StopWatch;
import com.group38.ojcodesandbox.model.ExecuteMessage;

import java.io.BufferedReader;
import java.io.InputStreamReader;

//进程工具类

public class ProcessUtils {
    //执行进程并获取信息

    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            //等待执行程序，获取错误码
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);

            if (exitValue == 0) {
                //正常退出
                System.out.println(opName + "成功");

                //分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                StringBuilder compileOutputStringBuilder = new StringBuilder();

                //逐行获取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    compileOutputStringBuilder.append(compileOutputLine);
                }
                executeMessage.setMessage(compileOutputStringBuilder.toString());
            } else {
                //异常退出
                System.out.println(opName + "失败， 错误码：" + exitValue);

                //分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                StringBuilder compileOutputStringBuilder = new StringBuilder();

                //逐行获取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    compileOutputStringBuilder.append(compileOutputLine);
                }
                executeMessage.setMessage(compileOutputStringBuilder.toString());

                //分批获取程序的正常输出
                BufferedReader errorbufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                StringBuilder errorcompileOutputStringBuilder = new StringBuilder();

                //逐行获取
                String errorcompileOutputLine;
                while ((errorcompileOutputLine = errorbufferedReader.readLine()) != null) {
                    errorcompileOutputStringBuilder.append(errorcompileOutputLine);
                }
                executeMessage.setErrorMessage(errorcompileOutputStringBuilder.toString());
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeMessage;
    }
}
