package com.group38.oj.judge;

import com.group38.oj.judge.strategy.DefaultStrategy;
import com.group38.oj.judge.strategy.JavaStrategy;
import com.group38.oj.judge.strategy.JudgeContext;
import com.group38.oj.judge.strategy.JudgeStrategy;
import com.group38.oj.judge.sandbox.model.JudgeInfo;
import com.group38.oj.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

// 判题管理器
@Service
public class JudgeManager {

    JudgeInfo exec(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String lang = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultStrategy();
        if ("Java".equals(lang)) {
            judgeStrategy = new JavaStrategy();
        }
        return judgeStrategy.execjudge(judgeContext);
    }
}
