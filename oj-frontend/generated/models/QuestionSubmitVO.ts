/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { JudgeInfo } from './JudgeInfo';
import type { QuestionVO } from './QuestionVO';
import type { UserVO } from './UserVO';
export type QuestionSubmitVO = {
    code?: string;
    createTime?: string;
    id?: string;
    judgeInfo?: JudgeInfo;
    language?: string;
    questionId?: string;
    questionVO?: QuestionVO;
    status?: number;
    updateTime?: string;
    userId?: string;
    userVO?: UserVO;
};

