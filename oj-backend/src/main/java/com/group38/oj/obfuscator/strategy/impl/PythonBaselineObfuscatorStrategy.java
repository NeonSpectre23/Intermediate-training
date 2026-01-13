package com.group38.oj.obfuscator.strategy.impl;

import com.group38.oj.obfuscator.strategy.AbstractObfuscatorStrategy;
import org.springframework.stereotype.Component;

// Python Baseline混淆策略实现
@Component
public class PythonBaselineObfuscatorStrategy extends AbstractObfuscatorStrategy {
    
    @Override
    protected String buildObfuscateCommand(String language, String scheme, 
                                         String inputFilePath, String outputFilePath) {
        // 获取python工具目录
        String pythonToolsDir = TOOLS_ROOT_DIR + "python";
        // 构建调用baseline.py的命令
        // 这里我们创建一个临时的Python脚本作为包装器
        String wrapperScript = "import sys\nsys.path.append(r'" + pythonToolsDir + "')\nfrom baseline import CodeObfuscator\n\nwith open(r'" + inputFilePath + "', 'r') as f:\n    code = f.read()\n\nobfuscator = CodeObfuscator()\nobfuscated_code = obfuscator.obfuscate(code)\n\nwith open(r'" + outputFilePath + "', 'w') as f:\n    f.write(obfuscated_code)\n";
        
        // 保存包装脚本到临时文件
        try {
            java.io.File wrapperFile = java.io.File.createTempFile("python_wrapper_" + System.currentTimeMillis(), ".py");
            java.nio.file.Files.write(wrapperFile.toPath(), wrapperScript.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            wrapperFile.deleteOnExit();
            
            // 构建命令
            return "python " + wrapperFile.getAbsolutePath() + " " + inputFilePath + " " + outputFilePath;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create wrapper script: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getSupportedLanguage() {
        return "python";
    }
    
    @Override
    public String getSupportedScheme() {
        return "baseline";
    }
}
