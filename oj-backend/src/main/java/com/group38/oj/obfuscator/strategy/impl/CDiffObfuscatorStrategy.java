package com.group38.oj.obfuscator.strategy.impl;

import com.group38.oj.obfuscator.strategy.AbstractObfuscatorStrategy;
import org.springframework.stereotype.Component;

// C语言Diff混淆策略实现
@Component
public class CDiffObfuscatorStrategy extends AbstractObfuscatorStrategy {
    
    @Override
    protected String buildObfuscateCommand(String language, String scheme, 
                                         String inputFilePath, String outputFilePath) {
        // 构建调用c_obf_diff.py的命令
        String cObfDiffPath = TOOLS_ROOT_DIR + "c\\c_obf_diff.py";
        return String.format("python \"%s\" \"%s\" -o \"%s\"", 
                           cObfDiffPath, inputFilePath, outputFilePath);
    }
    
    @Override
    public String getSupportedLanguage() {
        return "c";
    }
    
    @Override
    public String getSupportedScheme() {
        return "diff";
    }
}
