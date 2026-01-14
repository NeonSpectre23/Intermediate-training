package com.group38.oj.judge.sandbox.impl;

import com.group38.oj.judge.sandbox.Sandbox;
import com.group38.oj.judge.sandbox.model.ExecCodeRequest;
import com.group38.oj.judge.sandbox.model.ExecCodeResponse;
import com.group38.oj.judge.sandbox.model.JudgeInfo;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// 远程沙箱（调用Judge0 API）
public class RemoteSandbox implements Sandbox {

    // Judge0 API地址
    private static final String JUDGE0_API_URL = "https://extra-ce.judge0.com/submissions/";
    // Judge0语言列表API
    private static final String JUDGE0_LANGUAGES_URL = "https://extra-ce.judge0.com/languages";
    // 语言ID映射，将我们的语言名称映射到Judge0的语言ID
    private static Map<String, Integer> LANGUAGE_ID_MAP = new HashMap<>();
    
    // 初始化默认语言ID映射
    static {
        // 设置默认映射，同时添加常见的Python版本映射作为备选
        Map<String, Integer> defaultMap = new HashMap<>();
        defaultMap.put("java", 62);       // Java (OpenJDK 17.0.8)
        defaultMap.put("python", 71);     // Python 3.8（常见Judge0支持）
        defaultMap.put("python3", 71);    // Python 3.8（常见Judge0支持）
        defaultMap.put("python3.8", 71);  // Python 3.8
        defaultMap.put("python3.10", 76); // Python 3.10
        defaultMap.put("python3.11", 92); // Python 3.11
        defaultMap.put("python2", 68);    // Python 2.7.18
        defaultMap.put("c", 65);          // C (GCC 12.2.0)
        defaultMap.put("cpp", 64);        // C++ (GCC 12.2.0)
        defaultMap.put("c++", 64);        // C++ (GCC 12.2.0) - 别名
        defaultMap.put("csharp", 69);     // C# (Mono 6.12.0.182)
        defaultMap.put("c#", 69);         // C# (Mono 6.12.0.182) - 别名
        defaultMap.put("go", 60);         // Go (1.20.3)
        defaultMap.put("javascript", 91);  // JavaScript (Node.js 18.15.0)
        defaultMap.put("typescript", 92);  // TypeScript (5.0.3)
        defaultMap.put("rust", 73);       // Rust (1.71.0)
        
        // 尝试动态获取当前Judge0实例支持的语言列表
        try {
            LANGUAGE_ID_MAP = getSupportedLanguages();
            if (LANGUAGE_ID_MAP.isEmpty()) {
                // 如果动态获取失败，使用默认映射
                LANGUAGE_ID_MAP = defaultMap;
            }
        } catch (Exception e) {
            // 如果获取语言列表失败，使用默认映射
            System.err.println("获取Judge0支持的语言列表失败，使用默认映射：" + e.getMessage());
            LANGUAGE_ID_MAP = defaultMap;
        }
    }

    @Override
    public ExecCodeResponse execCode(ExecCodeRequest execCodeRequest) {
        RestTemplate restTemplate = new RestTemplate();
        List<String> inputList = execCodeRequest.getInputList();
        
        // 如果没有输入用例，直接返回空响应
        if (inputList == null || inputList.isEmpty()) {
            return getErrorResponse("没有输入用例");
        }
        
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // 对source_code进行Base64编码
        String sourceCode = execCodeRequest.getCode();
        String base64SourceCode = Base64.getEncoder().encodeToString(sourceCode.getBytes());
        
        // 获取语言ID
        String language = execCodeRequest.getLang();
        Integer languageId;
        if (language == null || language.isEmpty()) {
            // 默认使用Java
            languageId = 62;
        } else {
            languageId = LANGUAGE_ID_MAP.get(language.toLowerCase());
            if (languageId == null) {
                // 默认使用Java
                languageId = 62;
            }
        }
        
        // 设置wait=true和base64_encoded=true
        String requestUrl = JUDGE0_API_URL + "?wait=true&base64_encoded=true";
        
        // 初始化最终响应
        ExecCodeResponse finalResponse = new ExecCodeResponse();
        finalResponse.setOutputList(new ArrayList<>());
        finalResponse.setJudgeInfo(new JudgeInfo());
        
        // 初始化统计信息
        long totalTime = 0;
        long maxMemory = 0;
        boolean allAccepted = true;
        
        try {
            // 为每个测试用例单独调用Judge0 API
            for (String input : inputList) {
                // 构建Judge0请求体
                Map<String, Object> judge0Request = new HashMap<>();
                judge0Request.put("source_code", base64SourceCode);
                judge0Request.put("language_id", languageId);
                
                // 对单个输入进行Base64编码
                String base64Stdin = Base64.getEncoder().encodeToString(input.getBytes());
                judge0Request.put("stdin", base64Stdin);
                
                // 设置内存限制为128MB（128000KB）
                judge0Request.put("memory_limit", 128000);
                // 设置时间限制为5秒
                judge0Request.put("cpu_time_limit", 5.0);
                
                // 设置请求体
                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(judge0Request, headers);
                
                // 发送POST请求
                ResponseEntity<Map> responseEntity = restTemplate.postForEntity(
                        requestUrl,
                        requestEntity,
                        Map.class
                );
                
                Map<String, Object> judge0Response = responseEntity.getBody();
                if (judge0Response == null) {
                    return getErrorResponse("Judge0 API返回空响应");
                }
                
                // 转换为ExecCodeResponse格式
                ExecCodeResponse testCaseResponse = convertToExecCodeResponse(judge0Response);
                
                // 更新统计信息
                if (testCaseResponse.getStatus() != 1) {
                    allAccepted = false;
                }
                
                // 累加时间和最大内存
                if (testCaseResponse.getJudgeInfo() != null) {
                    Long time = testCaseResponse.getJudgeInfo().getTime();
                    if (time != null) {
                        totalTime += time;
                    }
                    Long memory = testCaseResponse.getJudgeInfo().getMemory();
                    if (memory != null && memory > maxMemory) {
                        maxMemory = memory;
                    }
                }
                
                // 添加输出结果
                if (testCaseResponse.getOutputList() != null && !testCaseResponse.getOutputList().isEmpty()) {
                    finalResponse.getOutputList().addAll(testCaseResponse.getOutputList());
                } else {
                    // 确保每个测试用例至少有一个输出，避免后续判题策略因为输出数量不匹配而判为WA
                    finalResponse.getOutputList().add("");
                }
            }
            
            // 设置最终状态
            if (allAccepted) {
                finalResponse.setStatus(1); // 1表示成功
                finalResponse.setMessage("Accepted");
            } else {
                finalResponse.setStatus(3); // 3表示失败
                finalResponse.setMessage("Wrong Answer");
            }
            
            // 设置最终统计信息
            finalResponse.getJudgeInfo().setTime(totalTime);
            finalResponse.getJudgeInfo().setMemory(maxMemory);
            
            return finalResponse;
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = e.getMessage();
            
            // 特殊处理语言ID错误
            if (errorMessage != null && (errorMessage.contains("language with id") || errorMessage.contains("422") || errorMessage.contains("Unprocessable Entity"))) {
                // 语言ID错误，返回系统错误，而不是WA
                return getSystemErrorResponse("Judge0 API不支持该语言ID：" + languageId + "，错误信息：" + errorMessage);
            }
            
            // 确保输出列表不为空，避免后续判题策略因为输出数量不匹配而判为WA
            for (int i = 0; i < inputList.size(); i++) {
                finalResponse.getOutputList().add("");
            }
            
            return getErrorResponse("调用Judge0 API失败：" + errorMessage);
        }
    }

    /**
     * 将Judge0响应转换为ExecCodeResponse格式
     */
    private ExecCodeResponse convertToExecCodeResponse(Map<String, Object> judge0Response) {
        ExecCodeResponse execCodeResponse = new ExecCodeResponse();
        execCodeResponse.setOutputList(new ArrayList<>()); // 初始化outputList
        JudgeInfo judgeInfo = new JudgeInfo();

        // 解析状态
        Map<String, Object> status = (Map<String, Object>) judge0Response.get("status");
        if (status != null) {
            Integer statusId = (Integer) status.get("id");
            // 映射Judge0状态到我们的状态码
            // 3: Accepted, 4: Wrong Answer, 5: Time Limit Exceeded, 6: Compilation Error
            // 7-12: Runtime Error, 13: Internal Error
            if (statusId == 3) {
                execCodeResponse.setStatus(1); // 1表示成功
            } else {
                execCodeResponse.setStatus(3); // 3表示失败
            }
            execCodeResponse.setMessage((String) status.get("description"));
        }

        // 解析输出
        String stdout = (String) judge0Response.get("stdout");
        if (stdout != null) {
            try {
                // 对stdout进行Base64解码
                byte[] decodedBytes = Base64.getDecoder().decode(stdout);
                String decodedStdout = new String(decodedBytes);
                // 将stdout按换行分割为输出列表，并过滤掉空字符串
                String[] outputs = decodedStdout.split("\n");
                for (String output : outputs) {
                    if (!output.isEmpty()) {
                        execCodeResponse.getOutputList().add(output);
                    }
                }
            } catch (IllegalArgumentException e) {
                // 如果不是Base64编码，直接使用，并过滤掉空字符串
                String[] outputs = stdout.split("\n");
                for (String output : outputs) {
                    if (!output.isEmpty()) {
                        execCodeResponse.getOutputList().add(output);
                    }
                }
            }
        }

        // 解析编译输出和错误输出
        String compileOutput = (String) judge0Response.get("compile_output");
        String stderr = (String) judge0Response.get("stderr");
        String message = (String) judge0Response.get("message");
        
        StringBuilder errorMessage = new StringBuilder();
        if (compileOutput != null) {
            try {
                // 对compile_output进行Base64解码
                byte[] decodedBytes = Base64.getDecoder().decode(compileOutput);
                errorMessage.append(new String(decodedBytes)).append("\n");
            } catch (IllegalArgumentException e) {
                // 如果不是Base64编码，直接使用
                errorMessage.append(compileOutput).append("\n");
            }
        }
        if (stderr != null) {
            try {
                // 对stderr进行Base64解码
                byte[] decodedBytes = Base64.getDecoder().decode(stderr);
                errorMessage.append(new String(decodedBytes)).append("\n");
            } catch (IllegalArgumentException e) {
                // 如果不是Base64编码，直接使用
                errorMessage.append(stderr).append("\n");
            }
        }
        if (message != null) {
            errorMessage.append(message).append("\n");
        }
        
        if (errorMessage.length() > 0) {
            execCodeResponse.setMessage(errorMessage.toString());
        }

        // 解析执行时间和内存
        Object timeObj = judge0Response.get("time");
        if (timeObj != null) {
            try {
                double time;
                if (timeObj instanceof String) {
                    time = Double.parseDouble((String) timeObj);
                } else if (timeObj instanceof Number) {
                    time = ((Number) timeObj).doubleValue();
                } else {
                    time = 0;
                }
                judgeInfo.setTime((long) (time * 1000)); // 转换为毫秒
            } catch (Exception e) {
                // 忽略解析错误
            }
        }
        
        // 解析内存
        Object memoryObj = judge0Response.get("memory");
        if (memoryObj != null) {
            try {
                long memory;
                if (memoryObj instanceof String) {
                    memory = Long.parseLong((String) memoryObj);
                } else if (memoryObj instanceof Number) {
                    memory = ((Number) memoryObj).longValue();
                } else {
                    memory = 0;
                }
                judgeInfo.setMemory(memory); // Judge0返回的是KB，转换为Long类型
            } catch (Exception e) {
                // 忽略解析错误
            }
        }

        execCodeResponse.setJudgeInfo(judgeInfo);
        return execCodeResponse;
    }

    /**
     * 获取错误响应
     */
    private ExecCodeResponse getErrorResponse(String message) {
        ExecCodeResponse execCodeResponse = new ExecCodeResponse();
        execCodeResponse.setStatus(3); // 3表示代码错误（WA）
        execCodeResponse.setMessage(message);
        execCodeResponse.setOutputList(new ArrayList<>()); // 初始化outputList
        execCodeResponse.setJudgeInfo(new JudgeInfo());
        return execCodeResponse;
    }
    
    /**
     * 获取系统错误响应
     */
    private ExecCodeResponse getSystemErrorResponse(String message) {
        ExecCodeResponse execCodeResponse = new ExecCodeResponse();
        execCodeResponse.setStatus(2); // 2表示系统错误
        execCodeResponse.setMessage(message);
        execCodeResponse.setOutputList(new ArrayList<>()); // 初始化outputList
        execCodeResponse.setJudgeInfo(new JudgeInfo());
        return execCodeResponse;
    }
    
    /**
     * 动态获取Judge0支持的语言列表
     * @return 语言ID映射
     */
    private static Map<String, Integer> getSupportedLanguages() {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Integer> languageMap = new HashMap<>();
        
        try {
            // 发送GET请求获取语言列表
            ResponseEntity<List> responseEntity = restTemplate.getForEntity(
                    JUDGE0_LANGUAGES_URL,
                    List.class
            );
            
            List<Map<String, Object>> languages = responseEntity.getBody();
            if (languages == null) {
                System.err.println("获取Judge0语言列表返回空");
                return languageMap;
            }
            
            System.out.println("获取到Judge0支持的语言列表，共" + languages.size() + "种语言");
            
            // 遍历语言列表，构建映射
            for (Map<String, Object> language : languages) {
                Integer id = (Integer) language.get("id");
                String name = (String) language.get("name");
                String slug = (String) language.get("slug");
                
                if (id == null || name == null) {
                    continue;
                }
                
                System.out.println("语言: " + name + " (slug: " + slug + ", id: " + id + ")");
                
                // 添加多种映射方式
                if (name != null) {
                    // 精确匹配
                    languageMap.put(name.toLowerCase(), id);
                    
                    // 处理Python版本
                    if (name.startsWith("Python")) {
                        // 添加python映射
                        languageMap.put("python", id);
                        languageMap.put("python3", id);
                        
                        // 提取版本号，如Python 3.8 -> python3.8
                        String[] nameParts = name.split(" ");
                        if (nameParts.length > 1) {
                            String version = nameParts[1].toLowerCase();
                            languageMap.put("python" + version.replace(".", ""), id);
                            languageMap.put("python" + version, id);
                        }
                    }
                }
                
                // 基于slug的映射
                if (slug != null) {
                    languageMap.put(slug.toLowerCase(), id);
                    
                    // 处理python slug
                    if (slug.startsWith("python")) {
                        languageMap.put("python", id);
                        languageMap.put("python3", id);
                    }
                }
            }
            
            System.out.println("构建的语言ID映射: " + languageMap);
            
        } catch (Exception e) {
            System.err.println("获取Judge0语言列表失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return languageMap;
    }
}
