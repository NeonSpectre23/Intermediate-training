package com.group38.ojcodesandbox;

import com.group38.ojcodesandbox.model.ExecCodeRequest;
import com.group38.ojcodesandbox.model.ExecCodeResponse;
import org.springframework.stereotype.Component;


//Java原生代码沙箱实现，直接调用模板方法

@Component
public class JavaSandBox extends JavaSandBoxTemplate {
    @Override
    public ExecCodeResponse execCode(ExecCodeRequest execCoderequest) {
        return super.execCode(execCoderequest);
    }
}
