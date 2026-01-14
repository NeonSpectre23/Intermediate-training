package com.group38.oj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.group38.oj.model.entity.Question;
import com.group38.oj.model.entity.QuestionFavour;
import com.group38.oj.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 题目收藏服务
 *
 */
public interface QuestionFavourService extends IService<QuestionFavour> {

    /**
     * 题目收藏
     *
     * @param questionId 题目id
     * @param loginUser 登录用户
     * @return 变动数
     */
    int doQuestionFavour(long questionId, User loginUser);

    /**
     * 分页获取用户收藏的题目列表
     *
     * @param page 分页参数
     * @param queryWrapper 查询条件
     * @param userId 用户id
     * @return 题目分页
     */
    Page<Question> listFavourQuestionByPage(Page<Question> page, QueryWrapper<Question> queryWrapper, long userId);
}
