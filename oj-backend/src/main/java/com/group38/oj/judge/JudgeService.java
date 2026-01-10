package com.group38.oj.judge;

import com.group38.oj.model.entity.QuestionSubmit;

// 判题服务
public interface JudgeService {

    // 判题
    QuestionSubmit judge(long questionSubmitId);
}
