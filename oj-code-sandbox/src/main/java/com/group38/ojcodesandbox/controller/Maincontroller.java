package com.group38.ojcodesandbox.controller;

import com.group38.ojcodesandbox.JavaSandBox;
import com.group38.ojcodesandbox.model.ExecCodeRequest;
import com.group38.ojcodesandbox.model.ExecCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/")
@Slf4j
public class Maincontroller {

    //定义鉴权请求头和密钥
    public static final String AUTH_REQUEST_HEADER = "auth";
    public static final String AUTH_REQUEST_SECRET = "secretKey";

    @Autowired
    private JavaSandBox javaSandBox;

    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }

    //执行代码
    @PostMapping("/execuCode")
    ExecCodeResponse execCodeResponse(@RequestBody ExecCodeRequest execCodeRequest, HttpServletRequest request, HttpServletResponse response){
        // 调试日志：记录HTTP请求输入
        log.info("收到execuCode请求，参数：{}", execCodeRequest);
        
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        log.info("认证头：{}", authHeader);

        //基本的认证
        if (!AUTH_REQUEST_SECRET.equals(authHeader)){
            log.warn("认证失败，请求头：{}", authHeader);
            response.setStatus(403);
            return null;
        }
        if (execCodeRequest == null){
            log.error("请求参数为空");
            throw new RuntimeException("请求参数为空");
        }
        
        ExecCodeResponse result = javaSandBox.execCode(execCodeRequest);
        // 调试日志：记录HTTP响应输出
        log.info("execuCode响应：{}", result);
        
        return result;
    }
}
