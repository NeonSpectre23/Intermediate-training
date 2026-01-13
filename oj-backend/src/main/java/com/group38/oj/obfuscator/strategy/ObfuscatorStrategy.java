package com.group38.oj.obfuscator.strategy;

public interface ObfuscatorStrategy {
    
    /**
     * 执行代码混淆
     * @param obfuscatorContext 混淆上下文
     * @return 混淆后的代码
     */
    String obfuscate(ObfuscatorContext obfuscatorContext);
    
    /**
     * 获取支持的语言
     * @return 语言名称
     */
    String getSupportedLanguage();
    
    /**
     * 获取支持的混淆方案
     * @return 混淆方案名称
     */
    String getSupportedScheme();
}
