package com.group38.oj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.group38.oj.model.dto.question.JudgeCase;
import com.group38.oj.model.dto.question.JudgeConfig;
import com.group38.oj.judge.sandbox.model.JudgeInfo;
import com.group38.oj.model.entity.Question;
import com.group38.oj.model.enums.JudgeInfoMessageEnum;

import java.util.List;

// 默认判题策略
public class JavaStrategy implements JudgeStrategy {

    @Override
    public JudgeInfo execjudge(JudgeContext judgeContext) {

        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        Long memory = judgeInfo.getMemory();
        Long time = judgeInfo.getTime();
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        List<JudgeCase> list = judgeContext.getJudgeCaseList();

        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;
        JudgeInfo judgeInfoResponse = new JudgeInfo();

        judgeInfoResponse.setMemory(memory);
        judgeInfoResponse.setTime(time);

        if (outputList.size() != inputList.size()) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }

        for (int i = 0; i < list.size(); i++) {
            String expectedOutput = list.get(i).getOutput().trim();
            String actualOutput = outputList.get(i).trim();
            
            // 增强比较逻辑：处理空白字符和换行符
            expectedOutput = expectedOutput.replaceAll("\\s+", " ").trim();
            actualOutput = actualOutput.replaceAll("\\s+", " ").trim();
            
            if (!expectedOutput.equals(actualOutput)) {
                judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
                return judgeInfoResponse;
            }
        }

        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);

        // JAVA 语言判题
        long JAVA_TIME_LIMIT = 1000L;
        if (time - JAVA_TIME_LIMIT > judgeConfig.getTimeLimit()) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        if (memory > judgeConfig.getMemoryLimit()) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }

        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
        return judgeInfoResponse;
    }
}
