package com.group38.oj.judge.sandbox;

import com.group38.oj.judge.sandbox.model.ExecCodeRequest;
import com.group38.oj.judge.sandbox.model.ExecCodeResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SandboxProxy implements Sandbox {

    private final Sandbox sandbox;

    public SandboxProxy(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    @Override
    public ExecCodeResponse execCode(ExecCodeRequest execCoderequest) {
        log.info("代码沙箱请求信息： " + execCoderequest.toString());
        ExecCodeResponse execCodeResponse = sandbox.execCode(execCoderequest);
        log.info("代码沙箱响应信息： " + execCodeResponse.toString());
        return execCodeResponse;
    }
}
