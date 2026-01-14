package com.group38.ojcodesandbox;


import com.group38.ojcodesandbox.model.ExecCodeRequest;
import com.group38.ojcodesandbox.model.ExecCodeResponse;

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
