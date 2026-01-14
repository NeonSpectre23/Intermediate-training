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
import com.group38.oj.judge.sandbox.model.JudgeInfo;
import com.group38.oj.model.entity.Question;
import com.group38.oj.model.entity.QuestionSubmit;
import com.group38.oj.model.enums.JudgeInfoMessageEnum;
import com.group38.oj.model.enums.QuestionSubmitStatusEnum;
import com.group38.oj.service.QuestionService;
import com.group38.oj.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JudgeServiceImpl implements JudgeService {

    private static final Logger log = LoggerFactory.getLogger(JudgeServiceImpl.class);

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
        
        // 调试日志：记录判题基本信息
        log.info("开始判题 - 提交ID: {}, 题目ID: {}, 语言: {}", questionSubmitId, questionId, lang);
        log.info("执行代码: {}", code);
        log.info("测试用例数: {}", list.size());
        
        for (int i = 0; i < list.size(); i++) {
            JudgeCase judgeCase = list.get(i);
            log.info("测试用例 {} - 输入: '{}', 预期输出: '{}'", i+1, judgeCase.getInput(), judgeCase.getOutput());
        }
        
        ExecCodeRequest execCoderequest = ExecCodeRequest.builder()
                .code(code)
                .lang(lang)
                .inputList(inputList)
                .build();
        ExecCodeResponse execCodeResponse = sandbox.execCode(execCoderequest);
        List<String> outputList = execCodeResponse.getOutputList();
        
        // 调试日志：记录执行结果
        log.info("执行结果 - 输出数: {}", outputList.size());
        for (int i = 0; i < outputList.size(); i++) {
            log.info("执行结果 {} - 实际输出: '{}'", i+1, outputList.get(i));
            log.info("执行结果 {} - 实际输出长度: {}, 字符编码: {}", i+1, outputList.get(i).length(), outputList.get(i).getBytes().length);
            // 输出字符的详细信息
            char[] chars = outputList.get(i).toCharArray();
            StringBuilder charInfo = new StringBuilder();
            for (char c : chars) {
                charInfo.append(String.format("%c(0x%04x), ", c, (int) c));
            }
            log.info("执行结果 {} - 字符详细信息: [{}]", i+1, charInfo.toString());
        }

        // 调试日志：记录执行结果状态
        log.info("沙箱执行结果状态: {}, 消息: {}", execCodeResponse.getStatus(), execCodeResponse.getMessage());
        
        // 检查沙箱执行是否返回系统错误
        if (execCodeResponse.getStatus() == 2) {
            // 系统错误，直接返回系统错误结果，不进行后续判题
            log.error("沙箱执行系统错误: {}", execCodeResponse.getMessage());
            
            // 更新题目提交状态为系统错误
            questionSubmitUpdate = new QuestionSubmit();
            questionSubmitUpdate.setId(questionSubmitId);
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.REJECTED.getValue());
            
            // 创建系统错误的JudgeInfo
            JudgeInfo systemErrorJudgeInfo = new JudgeInfo();
            systemErrorJudgeInfo.setMessage("System Error: " + execCodeResponse.getMessage());
            systemErrorJudgeInfo.setTime(0L);
            systemErrorJudgeInfo.setMemory(0L);
            
            questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(systemErrorJudgeInfo));
            update = questionSubmitService.updateById(questionSubmitUpdate);
            if (!update) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目状态更新失败");
            }
        } else {
            // 正常执行，进入判题策略
            JudgeContext judgeContext = new JudgeContext();
            judgeContext.setJudgeInfo(execCodeResponse.getJudgeInfo());
            judgeContext.setInputList(inputList);
            judgeContext.setOutputList(outputList);
            judgeContext.setQuestion(question);
            judgeContext.setQuestionSubmit(questionSubmit);
            judgeContext.setJudgeCaseList(list);

            JudgeInfo judgeInfo = judgeManager.exec(judgeContext);
            
            // 调试日志：记录判题结果
            log.info("判题完成 - 结果: {}, 运行时间: {}ms, 内存: {}KB", 
                    judgeInfo.getMessage(), judgeInfo.getTime(), judgeInfo.getMemory());

            // 更新题目提交状态
            questionSubmitUpdate = new QuestionSubmit();
            questionSubmitUpdate.setId(questionSubmitId);
            // 根据判题结果设置状态
            Integer status;
            if (JudgeInfoMessageEnum.ACCEPTED.getValue().equals(judgeInfo.getMessage())) {
                status = QuestionSubmitStatusEnum.ACCEPTED.getValue();
            } else {
                status = QuestionSubmitStatusEnum.REJECTED.getValue();
            }
            questionSubmitUpdate.setStatus(status);
            questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
            update = questionSubmitService.updateById(questionSubmitUpdate);
            if (!update) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目状态更新失败");
            }
        }

        // 更新题目表中的提交数和通过数
        Question questionUpdate = new Question();
        questionUpdate.setId(questionId);
        // 提交数加1
        questionUpdate.setSubmitNum(question.getSubmitNum() + 1);
        // 如果通过了，通过数加1
        if (questionSubmitUpdate.getStatus().equals(QuestionSubmitStatusEnum.ACCEPTED.getValue())) {
            questionUpdate.setAcceptedNum(question.getAcceptedNum() + 1);
        }
        boolean questionUpdateResult = questionService.updateById(questionUpdate);
        if (!questionUpdateResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目提交数更新失败");
        }
        return questionSubmitService.getById(questionSubmitId);
    }
}
