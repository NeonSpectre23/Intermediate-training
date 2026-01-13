package com.group38.oj.obfuscator.strategy.impl;

import com.group38.oj.obfuscator.strategy.AbstractObfuscatorStrategy;
import org.springframework.stereotype.Component;

// Python Diff混淆策略实现
@Component
public class PythonDiffObfuscatorStrategy extends AbstractObfuscatorStrategy {
    
    @Override
    protected String buildObfuscateCommand(String language, String scheme, 
                                         String inputFilePath, String outputFilePath) {
        // 构建调用py_obf_diff.py的命令
        String pyObfDiffPath = TOOLS_ROOT_DIR + "python\\py_obf_diff.py";
        return String.format("python \"%s\" -i \"%s\" -o \"%s\"", 
                           pyObfDiffPath, inputFilePath, outputFilePath);
    }
    
    @Override
    public String getSupportedLanguage() {
        return "python";
    }
    
    @Override
    public String getSupportedScheme() {
        return "diff";
    }
}
