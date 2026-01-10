package com.group38.oj.judge.strategy;

import com.group38.oj.model.dto.questionsubmit.JudgeInfo;

public interface JudgeStrategy {

    // 执行判题接口
    JudgeInfo execjudge(JudgeContext judgeContext);
}
