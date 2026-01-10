package com.group38.oj.judge.sandbox.impl;

import com.group38.oj.judge.sandbox.Sandbox;
import com.group38.oj.judge.sandbox.model.ExecCodeRequest;
import com.group38.oj.judge.sandbox.model.ExecCodeResponse;
import com.group38.oj.model.dto.questionsubmit.JudgeInfo;
import com.group38.oj.model.enums.JudgeInfoMessageEnum;
import com.group38.oj.model.enums.QuestionSubmitStatusEnum;

import java.util.List;

// 示例沙箱
public class ExampleSandbox implements Sandbox {
    @Override
    public ExecCodeResponse execCode(ExecCodeRequest execCoderequest) {
        List<String> inputList = execCoderequest.getInputList();

        ExecCodeResponse execCodeResponse = new ExecCodeResponse();
        execCodeResponse.setMessage("测试执行成功");
        execCodeResponse.setStatus(QuestionSubmitStatusEnum.ACCEPTED.getValue());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setMemory(100L);
        judgeInfo.setTime(100L);
        execCodeResponse.setJudgeInfo(judgeInfo);
        execCodeResponse.setOutputList(inputList);

        return null;
    }
}
