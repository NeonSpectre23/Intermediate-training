package com.group38.oj.judge.strategy;

import com.group38.oj.model.dto.question.JudgeCase;
import com.group38.oj.judge.sandbox.model.JudgeInfo;
import com.group38.oj.model.entity.Question;
import com.group38.oj.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

// 判题上下文，传递参数
@Data
public class JudgeContext {

    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private Question question;

    private QuestionSubmit questionSubmit;
    private List<JudgeCase> judgeCaseList;
}
