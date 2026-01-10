package com.group38.oj.judge.sandbox;

import com.group38.oj.judge.sandbox.model.ExecCodeRequest;
import com.group38.oj.judge.sandbox.model.ExecCodeResponse;

// 沙箱接口定义
public interface Sandbox {

    /**
     * 执行代码
     *
     * @param execCoderequest
     * @return
     */
    ExecCodeResponse execCode(ExecCodeRequest execCoderequest);
}
