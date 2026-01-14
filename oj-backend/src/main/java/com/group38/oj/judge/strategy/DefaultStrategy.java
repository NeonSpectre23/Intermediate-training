package com.group38.oj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.group38.oj.model.dto.question.JudgeCase;
import com.group38.oj.model.dto.question.JudgeConfig;
import com.group38.oj.judge.sandbox.model.JudgeInfo;
import com.group38.oj.model.entity.Question;
import com.group38.oj.model.enums.JudgeInfoMessageEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

// 默认判题策略
public class DefaultStrategy implements JudgeStrategy {

    private static final Logger log = LoggerFactory.getLogger(DefaultStrategy.class);

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

        // 调试日志：记录输入输出数量
        log.info("输入数量: {}, 输出数量: {}, 测试用例数量: {}", inputList.size(), outputList.size(), list.size());
        
        if (outputList.size() != inputList.size()) {
            log.error("输出数量与输入数量不匹配，预期: {}, 实际: {}", inputList.size(), outputList.size());
            judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }

        for (int i = 0; i < list.size(); i++) {
            String expectedOutput = list.get(i).getOutput().trim();
            
            // 处理实际输出：去除所有空白字符（包括换行符、制表符等），只保留内容
            String actualOutputRaw = outputList.get(i);
            // 1. 替换所有空白字符序列为单个空格
            // 2. 去除首尾空白
            // 3. 特别处理换行符问题
            String actualOutput = actualOutputRaw
                .replaceAll("\\s+", " ")  // 替换所有空白字符序列为单个空格
                .trim();  // 去除首尾空白
            
            // 同样处理预期输出，确保比较公平
            expectedOutput = expectedOutput
                .replaceAll("\\s+", " ")  // 替换所有空白字符序列为单个空格
                .trim();  // 去除首尾空白
            
            // 调试日志：记录比较详情
            log.info("测试用例 {} 比较开始:", i+1);
            log.info("原始预期输出: '{}'", list.get(i).getOutput());
            log.info("原始实际输出: '{}'", actualOutputRaw);
            log.info("处理后预期输出: '{}'", expectedOutput);
            log.info("处理后实际输出: '{}'", actualOutput);
            log.info("预期输出长度: {}, 实际输出长度: {}", expectedOutput.length(), actualOutput.length());
            
            // 详细比较每个字符
            if (expectedOutput.length() != actualOutput.length()) {
                log.error("输出长度不匹配，预期: {}, 实际: {}", expectedOutput.length(), actualOutput.length());
            } else {
                for (int j = 0; j < expectedOutput.length(); j++) {
                    char expectedChar = expectedOutput.charAt(j);
                    char actualChar = actualOutput.charAt(j);
                    if (expectedChar != actualChar) {
                        log.error("字符不匹配，位置: {}, 预期: '{}' (0x{:04x}), 实际: '{}' (0x{:04x})", 
                                 j, expectedChar, (int)expectedChar, actualChar, (int)actualChar);
                        break;
                    }
                }
            }
            
            if (!expectedOutput.equals(actualOutput)) {
                log.error("测试用例 {} 比较失败，预期: '{}', 实际: '{}'", i+1, expectedOutput, actualOutput);
                judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
                return judgeInfoResponse;
            } else {
                log.info("测试用例 {} 比较成功，预期: '{}', 实际: '{}'", i+1, expectedOutput, actualOutput);
            }
        }


        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);

        if (time > judgeConfig.getTimeLimit()) {
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
