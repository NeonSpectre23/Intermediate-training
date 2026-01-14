import message from "@arco-design/web-vue/es/message";

/**
 * 错误码映射，提供更友好的错误提示
 */
const errorCodeMap: Record<string, string> = {
  "400": "请求参数错误，请检查输入",
  "401": "未登录或登录已过期，请重新登录",
  "403": "没有权限执行此操作",
  "404": "请求的资源不存在",
  "500": "服务器内部错误，请稍后重试",
  "502": "网关错误，请稍后重试",
  "503": "服务暂时不可用，请稍后重试",
  "504": "请求超时，请稍后重试"
};

/**
 * 统一错误处理函数
 * @param error 错误对象
 * @param customMessage 自定义错误信息
 */
export const handleError = (error: any, customMessage?: string): void => {
  let messageContent = customMessage || "操作失败，请稍后重试";
  
  if (error.response) {
    // 服务器返回错误
    const { status, data } = error.response;
    messageContent = data.message || errorCodeMap[status.toString()] || `请求失败，状态码：${status}`;
  } else if (error.request) {
    // 请求发出但没有收到响应
    messageContent = "网络错误，请检查网络连接";
  } else {
    // 请求配置错误
    messageContent = error.message || "请求失败";
  }
  
  // 使用友好的错误提示组件
  message.error(messageContent);
};

/**
 * 成功提示
 * @param content 提示内容
 */
export const handleSuccess = (content = "操作成功"): void => {
  message.success(content);
};

/**
 * 警告提示
 * @param content 提示内容
 */
export const handleWarning = (content: string): void => {
  message.warning(content);
};

/**
 * 信息提示
 * @param content 提示内容
 */
export const handleInfo = (content: string): void => {
  message.info(content);
};
