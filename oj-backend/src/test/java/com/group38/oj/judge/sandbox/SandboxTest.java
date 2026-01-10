package com.group38.oj.judge.sandbox;

import com.group38.oj.judge.sandbox.impl.ExampleSandbox;
import com.group38.oj.judge.sandbox.impl.RemoteSandbox;
import com.group38.oj.judge.sandbox.model.ExecCodeRequest;
import com.group38.oj.judge.sandbox.model.ExecCodeResponse;
import com.group38.oj.model.enums.QuestionSubmitLanguageEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


@SpringBootTest
class SandboxTest {

    @Value("${sandbox.type:example}")
    private String type;

    @Test
    void executeCode() {
        Sandbox codeSandbox = new RemoteSandbox();
        String code = "int main() { }";
        String language = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2", "3 4");
        ExecCodeRequest execCodeRequest = ExecCodeRequest.builder()
                .code(code)
                .lang(language)
                .inputList(inputList)
                .build();
        ExecCodeResponse executeCodeResponse = codeSandbox.execCode(execCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }

    @Test
    void execCodeByValue() {
        Sandbox sandbox = SandboxFactory.newInstance(type);
        String code = "int main() { return 0; }";
        String lang = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2", "3 4");
        ExecCodeRequest execCoderequest = ExecCodeRequest.builder()
                .code(code)
                .lang(lang)
                .inputList(inputList)
                .build();
        ExecCodeResponse execCodeResponse = sandbox.execCode(execCoderequest);
        Assertions.assertNotNull(execCodeResponse);
    }

    @Test
    void execCodeByProxy() {
        Sandbox sandbox = SandboxFactory.newInstance(type);
        sandbox = new SandboxProxy(sandbox);
        String code = "int main() { return 0; }";
        String lang = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2", "3 4");
        ExecCodeRequest execCoderequest = ExecCodeRequest.builder()
                .code(code)
                .lang(lang)
                .inputList(inputList)
                .build();
        ExecCodeResponse execCodeResponse = sandbox.execCode(execCoderequest);
        Assertions.assertNotNull(execCodeResponse);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String type = scanner.next();
            Sandbox sandbox = SandboxFactory.newInstance(type);
            String code = "int main() { return 0; }";
            String lang = QuestionSubmitLanguageEnum.JAVA.getValue();
            List<String> inputList = Arrays.asList("1 2", "3 4");
            ExecCodeRequest execCoderequest = ExecCodeRequest.builder()
                    .code(code)
                    .lang(lang)
                    .inputList(inputList)
                    .build();
            ExecCodeResponse execCodeResponse = sandbox.execCode(execCoderequest);
        }
    }
}