package com.group38.oj.obfuscator.strategy;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;

// 抽象混淆策略基类，封装通用功能
public abstract class AbstractObfuscatorStrategy implements ObfuscatorStrategy {
    
    // 混淆工具脚本的根目录（从系统属性或环境变量获取，默认值作为 fallback）
    protected static final String TOOLS_ROOT_DIR = System.getProperty("obfuscator.tools.root") != null ? 
            System.getProperty("obfuscator.tools.root") : 
            (System.getenv("OBFUSCATOR_TOOLS_ROOT") != null ? 
             System.getenv("OBFUSCATOR_TOOLS_ROOT") : 
             "../Tools/");
    
    @Override
    public String obfuscate(ObfuscatorContext obfuscatorContext) {
        String sourceCode = obfuscatorContext.getSourceCode();
        String language = obfuscatorContext.getLanguage();
        String scheme = obfuscatorContext.getScheme();
        
        File tempFile = null;
        String inputFilePath = null;
        String outputFilePath = null;
        
        try {
            // 创建临时文件
            tempFile = createTempFile(language, sourceCode);
            inputFilePath = tempFile.getAbsolutePath();
            outputFilePath = inputFilePath + "_obf" + getFileExtension(language);
            
            // 调用具体的混淆命令
            String command = buildObfuscateCommand(language, scheme, inputFilePath, outputFilePath);
            executeCommand(command);
            
            // 读取混淆后的代码
            String obfuscatedCode = readFile(outputFilePath);
            
            return obfuscatedCode;
        } catch (Exception e) {
            throw new RuntimeException("Failed to obfuscate code: " + e.getMessage(), e);
        } finally {
            // 清理临时文件
            cleanupTempFiles(inputFilePath, outputFilePath);
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    
    /**
     * 创建临时文件
     * @param language 语言类型
     * @param content 文件内容
     * @return 临时文件对象
     * @throws IOException IO异常
     */
    protected File createTempFile(String language, String content) throws IOException {
        String extension = getFileExtension(language);
        File tempFile = File.createTempFile("obfuscate_input_" + System.currentTimeMillis(), extension);
        Files.write(tempFile.toPath(), content.getBytes(StandardCharsets.UTF_8), 
                   StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        tempFile.deleteOnExit();
        return tempFile;
    }
    
    /**
     * 获取文件扩展名
     * @param language 语言类型
     * @return 文件扩展名
     */
    protected String getFileExtension(String language) {
        switch (language.toLowerCase()) {
            case "python":
                return ".py";
            case "c":
                return ".c";
            default:
                return ".txt";
        }
    }
    
    /**
     * 构建混淆命令
     * @param language 语言类型
     * @param scheme 混淆方案
     * @param inputFilePath 输入文件路径
     * @param outputFilePath 输出文件路径
     * @return 混淆命令
     */
    protected abstract String buildObfuscateCommand(String language, String scheme, 
                                                  String inputFilePath, String outputFilePath);
    
    /**
     * 执行命令
     * @param command 命令
     * @throws IOException IO异常
     * @throws InterruptedException 中断异常
     */
    protected void executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("cmd.exe", "/c", command);
        processBuilder.redirectErrorStream(true);
        
        // 设置环境变量，确保Python输出使用UTF-8编码
        Map<String, String> env = processBuilder.environment();
        env.put("PYTHONIOENCODING", "UTF-8");
        
        Process process = processBuilder.start();
        
        // 读取命令输出
        readProcessOutput(process);
        
        // 等待命令执行完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Command execution failed with exit code: " + exitCode);
        }
    }
    
    /**
     * 读取进程输出
     * @param process 进程对象
     * @throws IOException IO异常
     */
    protected void readProcessOutput(Process process) throws IOException {
        // 读取标准输出和标准错误
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Obfuscator Output] " + line);
            }
        }
    }
    
    /**
     * 读取文件内容
     * @param filePath 文件路径
     * @return 文件内容
     * @throws IOException IO异常
     */
    protected String readFile(String filePath) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    /**
     * 清理临时文件
     * @param files 文件路径列表
     */
    protected void cleanupTempFiles(String... files) {
        if (files == null) {
            return;
        }
        
        for (String file : files) {
            if (file == null) {
                continue;
            }
            
            File f = new File(file);
            if (f.exists()) {
                boolean deleted = f.delete();
                if (!deleted) {
                    System.err.println("Failed to delete temp file: " + file);
                }
            }
        }
    }
}