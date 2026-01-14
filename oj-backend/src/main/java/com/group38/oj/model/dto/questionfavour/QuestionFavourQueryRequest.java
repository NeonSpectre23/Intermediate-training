package com.group38.oj.model.dto.questionfavour;

import com.group38.oj.model.dto.question.QuestionQueryRequest;
import java.io.Serializable;
import lombok.Data;

/**
 * 题目收藏查询请求
 *
 */
@Data
public class QuestionFavourQueryRequest implements Serializable {
    /**
     * 题目查询条件
     */
    private QuestionQueryRequest questionQueryRequest;

    /**
     * 用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
