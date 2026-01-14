package com.group38.oj.model.vo;

import cn.hutool.json.JSONUtil;
import com.group38.oj.model.dto.question.JudgeCase;
import com.group38.oj.model.dto.question.JudgeConfig;
import com.group38.oj.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目封装类
 *
 * @TableName question
 */
@Data
public class QuestionVO implements Serializable {
    /**
     * id
     */
    private String id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;

    /**
     * 判题用例（json 数组）
     */
    private List<JudgeCase> judgeCase;

    /**
     * 判题配置（json 对象）
     */
    private JudgeConfig judgeConfig;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建用户 id
     */
    private String userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 题目创建人信息
     */
    private UserVO userVO;

    /**
     * 包装类转对象
     *
     * @param questionVO
     * @return
     */
    public static Question voToObj(QuestionVO questionVO) {
        if (questionVO == null) {
            return null;
        }
        Question question = new Question();
        // 先复制非id字段
        BeanUtils.copyProperties(questionVO, question, "id", "userId");
        // 将String类型的id转换为Long类型
        if (questionVO.getId() != null) {
            question.setId(Long.parseLong(questionVO.getId()));
        }
        if (questionVO.getUserId() != null) {
            question.setUserId(Long.parseLong(questionVO.getUserId()));
        }
        List<String> tagList = questionVO.getTags();
        if (tagList != null) {
            question.setTags(JSONUtil.toJsonStr(tagList));
        }
        List<JudgeCase> judgeCaseList = questionVO.getJudgeCase();
        if (judgeCaseList != null) {
            question.setJudgeCase(JSONUtil.toJsonStr(judgeCaseList));
        }
        JudgeConfig judgeConfig = questionVO.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        return question;
    }

    /**
     * 对象转包装类
     *
     * @param question
     * @return
     */
    public static QuestionVO objToVo(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        // 将Long类型的id转换为String类型，避免JavaScript中的精度丢失问题
        questionVO.setId(String.valueOf(question.getId()));
        questionVO.setUserId(String.valueOf(question.getUserId()));
        List<String> tagList = JSONUtil.toList(question.getTags(), String.class);
        questionVO.setTags(tagList);
        String judgeCaseStr = question.getJudgeCase();
        questionVO.setJudgeCase(JSONUtil.toList(judgeCaseStr, JudgeCase.class));
        String judgeConfigStr = question.getJudgeConfig();
        questionVO.setJudgeConfig(JSONUtil.toBean(judgeConfigStr, JudgeConfig.class));
        return questionVO;
    }

    private static final long serialVersionUID = 1L;
}