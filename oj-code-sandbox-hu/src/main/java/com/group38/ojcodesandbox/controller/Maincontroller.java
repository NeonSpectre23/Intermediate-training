package com.group38.ojcodesandbox.controller;

import com.group38.ojcodesandbox.JavaSandBox;
import com.group38.ojcodesandbox.model.ExecCodeRequest;
import com.group38.ojcodesandbox.model.ExecCodeResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("/")

public class Maincontroller {

    //定义鉴权请求头和密钥
    public static final String AUTH_REQUEST_HEADER = "auth";
    public static final String AUTH_REQUEST_SECRET = "secretKey";

    @Resource
    private JavaSandBox javaSandBox;

    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }

    //执行代码
    @PostMapping("/execuCode")
    ExecCodeResponse execCodeResponse(@RequestBody ExecCodeRequest execCodeRequest, HttpServletRequest request, HttpServletResponse response){
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);

        //基本的认证
        if (!AUTH_REQUEST_SECRET.equals(authHeader)){
            response.setStatus(403);
            return null;
        }
        if (execCodeRequest == null){
            throw new RuntimeException("请求参数为空");
        }
        return javaSandBox.execCode(execCodeRequest);
    }
}
