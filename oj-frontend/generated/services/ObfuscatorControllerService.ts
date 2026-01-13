/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_ObfuscateCodeResponse_ } from "../models/BaseResponse_ObfuscateCodeResponse_";
import type { BaseResponse_SupportedSchemesResponse_ } from "../models/BaseResponse_SupportedSchemesResponse_";
import type { ObfuscateCodeRequest } from "../models/ObfuscateCodeRequest";
import type { CancelablePromise } from "../core/CancelablePromise";
import { OpenAPI } from "../core/OpenAPI";
import { request as __request } from "../core/request";
export class ObfuscatorControllerService {
  /**
   * obfuscateCode
   * @param request request
   * @returns BaseResponse_ObfuscateCodeResponse_ OK
   * @returns any Created
   * @throws ApiError
   */
  public static obfuscateCodeUsingPost(
    request: ObfuscateCodeRequest
  ): CancelablePromise<BaseResponse_ObfuscateCodeResponse_ | any> {
    return __request(OpenAPI, {
      method: "POST",
      url: "/api/obfuscator/obfuscate",
      body: request,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }
  /**
   * getSupportedSchemes
   * @returns BaseResponse_SupportedSchemesResponse_ OK
   * @throws ApiError
   */
  public static getSupportedSchemesUsingGet(): CancelablePromise<BaseResponse_SupportedSchemesResponse_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/obfuscator/schemes",
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }
}
