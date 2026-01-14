// Add a request interceptor
import axios from "axios";
import JSONBigInt from "json-bigint";

// 创建一个json-bigint实例，配置为将BigInt转换为字符串
const jsonBigInt = JSONBigInt({
  storeAsString: true
});

// 配置axios使用json-bigint来解析响应数据
axios.defaults.transformResponse = [function (data) {
  try {
    // 使用json-bigint来解析JSON数据，避免大数字精度丢失
    return jsonBigInt.parse(data);
  } catch (e) {
    // 如果解析失败，返回原始数据
    return data;
  }
}];

axios.defaults.withCredentials = true;
axios.interceptors.request.use(
  function (config) {
    // Do something before request is sent
    return config;
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error);
  }
);

// Add a response interceptor
axios.interceptors.response.use(
  function (response) {
    console.log("响应", response);
    console.log("响应完整数据:", response.data);
    console.log("响应状态:", response.status);
    console.log("响应头:", response.headers);
    // Any status code that lie within the range of 2xx cause this function to trigger
    // Do something with response data
    return response;
  },
  function (error) {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error
    console.error("响应错误完整信息:", error);
    if (error.response) {
      console.error("错误响应数据:", error.response.data);
      console.error("错误状态:", error.response.status);
      console.error("错误头:", error.response.headers);
    }
    return Promise.reject(error);
  }
);
