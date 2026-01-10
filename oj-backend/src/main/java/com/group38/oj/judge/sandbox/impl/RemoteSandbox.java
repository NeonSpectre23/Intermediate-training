package com.group38.oj.judge.sandbox.impl;

import com.group38.oj.judge.sandbox.Sandbox;
import com.group38.oj.judge.sandbox.model.ExecCodeRequest;
import com.group38.oj.judge.sandbox.model.ExecCodeResponse;

// 远程沙箱（调用开发的接口）
public class RemoteSandbox implements Sandbox {
    @Override
    public ExecCodeResponse execCode(ExecCodeRequest execCoderequest) {
        System.out.println("远程沙箱");
        return null;
    }
}
