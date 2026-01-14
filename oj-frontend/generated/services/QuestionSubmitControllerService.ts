/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_long_ } from "../models/BaseResponse_long_";
import type { BaseResponse_Page_QuestionSubmitVO_ } from "../models/BaseResponse_Page_QuestionSubmitVO_";
import type { QuestionSubmitAddRequest } from "../models/QuestionSubmitAddRequest";
import type { QuestionSubmitQueryRequest } from "../models/QuestionSubmitQueryRequest";
import type { CancelablePromise } from "../core/CancelablePromise";
import { OpenAPI } from '../core/OpenAPI';
import axios from 'axios';
import JSONBigInt from 'json-bigint';

// 创建一个json-bigint实例，配置为将BigInt转换为字符串
const jsonBigInt = JSONBigInt({
  storeAsString: true
});

// 配置axios使用json-bigint来解析响应数据
const axiosClient = axios.create();
axiosClient.defaults.transformResponse = [function (data) {
  try {
    // 使用json-bigint来解析JSON数据，避免大数字精度丢失
    return jsonBigInt.parse(data);
  } catch (e) {
    // 如果解析失败，返回原始数据
    return data;
  }
}];
import { request as __request } from "../core/request";
export class QuestionSubmitControllerService {
  /**
   * doQuestionSubmit
   * @param questionSubmitAddRequest questionSubmitAddRequest
   * @returns BaseResponse_long_ OK
   * @returns any Created
   * @throws ApiError
   */
  public static doQuestionSubmitUsingPost(
    questionSubmitAddRequest: QuestionSubmitAddRequest
  ): CancelablePromise<BaseResponse_long_ | any> {
    return __request(OpenAPI, {
      method: "POST",
      url: "/api/question_submit/",
      body: questionSubmitAddRequest,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    }, axiosClient);
  }
  /**
   * listQuestionSubmitByPage
   * @param questionSubmitQueryRequest questionSubmitQueryRequest
   * @returns BaseResponse_Page_QuestionSubmitVO_ OK
   * @returns any Created
   * @throws ApiError
   */
  public static listQuestionSubmitByPageUsingPost(
    questionSubmitQueryRequest: QuestionSubmitQueryRequest
  ): CancelablePromise<BaseResponse_Page_QuestionSubmitVO_ | any> {
    return __request(OpenAPI, {
      method: "POST",
      url: "/api/question_submit/list/page",
      body: questionSubmitQueryRequest,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    }, axiosClient);
  }
}
