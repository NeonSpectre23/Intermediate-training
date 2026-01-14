package com.group38.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.group38.oj.common.ErrorCode;
import com.group38.oj.exception.BusinessException;
import com.group38.oj.mapper.QuestionFavourMapper;
import com.group38.oj.model.entity.Question;
import com.group38.oj.model.entity.QuestionFavour;
import com.group38.oj.model.entity.User;
import com.group38.oj.service.QuestionFavourService;
import com.group38.oj.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目收藏服务实现
 *
 */
@Service
public class QuestionFavourServiceImpl extends ServiceImpl<QuestionFavourMapper, QuestionFavour> implements QuestionFavourService {

    @Resource
    private QuestionService questionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doQuestionFavour(long questionId, User loginUser) {
        // 判断题目是否存在
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        long userId = loginUser.getId();
        // 查询是否已经收藏
        QueryWrapper<QuestionFavour> questionFavourQueryWrapper = new QueryWrapper<>();
        questionFavourQueryWrapper.eq("questionId", questionId).eq("userId", userId);
        QuestionFavour oldFavour = this.getOne(questionFavourQueryWrapper);
        boolean result;
        if (oldFavour != null) {
            // 已收藏，取消收藏
            result = this.removeById(oldFavour.getId());
            if (result) {
                // 收藏数 - 1
                questionService.update()
                        .eq("id", questionId)
                        .gt("favourNum", 0)
                        .setSql("favourNum = favourNum - 1")
                        .update();
                return -1;
            } else {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "取消收藏失败");
            }
        } else {
            // 未收藏，添加收藏
            QuestionFavour questionFavour = new QuestionFavour();
            questionFavour.setQuestionId(questionId);
            questionFavour.setUserId(userId);
            result = this.save(questionFavour);
            if (result) {
                // 收藏数 + 1
                questionService.update()
                        .eq("id", questionId)
                        .setSql("favourNum = favourNum + 1")
                        .update();
                return 1;
            } else {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "添加收藏失败");
            }
        }
    }

    @Override
    public Page<Question> listFavourQuestionByPage(Page<Question> page, QueryWrapper<Question> queryWrapper, long userId) {
        // 获取我收藏的题目id列表
        QueryWrapper<QuestionFavour> questionFavourQueryWrapper = new QueryWrapper<>();
        questionFavourQueryWrapper.eq("userId", userId);
        List<QuestionFavour> questionFavourList = this.list(questionFavourQueryWrapper);
        if (CollectionUtils.isEmpty(questionFavourList)) {
            return new Page<>();
        }
        // 提取题目id
        List<Long> questionIdList = questionFavourList.stream()
                .map(QuestionFavour::getQuestionId)
                .collect(Collectors.toList());
        // 根据题目id查询题目
        queryWrapper.in("id", questionIdList);
        // 查询题目列表
        Page<Question> questionPage = questionService.page(page, queryWrapper);
        return questionPage;
    }
}
