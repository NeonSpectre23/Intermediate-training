package com.group38.oj.judge.sandbox;

import com.group38.oj.judge.sandbox.impl.ExampleSandbox;
import com.group38.oj.judge.sandbox.impl.RemoteSandbox;
import com.group38.oj.judge.sandbox.impl.ThirdPartySandbox;

// 沙箱工厂（根据字符串参数创建对应的代码沙箱示例）
public class SandboxFactory {

    /**
     * 根据字符串参数创建对应的代码沙箱示例
     *
     * @param type
     * @return
     */
    public static Sandbox newInstance(String type) {
        switch (type) {
            case "example":
                return new ExampleSandbox();
            case "remote":
                return new RemoteSandbox();
            case "third_party":
                return new ThirdPartySandbox();
            default:
                return new ExampleSandbox();
        }
    }
}
