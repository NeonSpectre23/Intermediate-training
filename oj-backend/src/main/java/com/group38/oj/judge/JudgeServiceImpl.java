package com.group38.oj.judge;

import cn.hutool.json.JSONUtil;
import com.group38.oj.common.ErrorCode;
import com.group38.oj.exception.BusinessException;
import com.group38.oj.judge.sandbox.Sandbox;
import com.group38.oj.judge.sandbox.SandboxFactory;
import com.group38.oj.judge.sandbox.SandboxProxy;
import com.group38.oj.judge.sandbox.model.ExecCodeRequest;
import com.group38.oj.judge.sandbox.model.ExecCodeResponse;
import com.group38.oj.judge.strategy.JudgeContext;
import com.group38.oj.model.dto.question.JudgeCase;
import com.group38.oj.model.dto.questionsubmit.JudgeInfo;
import com.group38.oj.model.entity.Question;
import com.group38.oj.model.entity.QuestionSubmit;
import com.group38.oj.model.enums.QuestionSubmitStatusEnum;
import com.group38.oj.service.QuestionService;
import com.group38.oj.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Value("${sandbox.type:example}")
    private String type;

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private JudgeManager judgeManager;

    @Override
    public QuestionSubmit judge(long questionSubmitId) {
        // 根据提交ID得到对应的提交信息和题目信息
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交记录不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }

        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "正在判题中，请勿重复判题");
        }
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目状态更新失败");
        }

        // 调用代码沙箱，获取输入用例和执行结果
        Sandbox sandbox = SandboxFactory.newInstance(type);
        sandbox = new SandboxProxy(sandbox);
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> list = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        String code = questionSubmit.getCode();
        String lang = questionSubmit.getLanguage();
        List<String> inputList = list.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecCodeRequest execCoderequest = ExecCodeRequest.builder()
                .code(code)
                .lang(lang)
                .inputList(inputList)
                .build();
        ExecCodeResponse execCodeResponse = sandbox.execCode(execCoderequest);
        List<String> outputList = execCodeResponse.getOutputList();

        // 根据执行结果对比用例输出，判断是否通过——>设置题目状态和信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(execCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setQuestion(question);
        judgeContext.setJudgeCaseList(list);
        judgeContext.setQuestionSubmit(questionSubmit);

        JudgeInfo judgeInfo = judgeManager.exec(judgeContext);

        // 更新题目状态
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.ACCEPTED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目状态更新失败");
        }
        return questionSubmitService.getById(questionId);
    }
}
