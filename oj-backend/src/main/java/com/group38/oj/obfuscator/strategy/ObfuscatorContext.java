package com.group38.oj.obfuscator.strategy;

import lombok.Data;

// 混淆上下文，传递参数
@Data
public class ObfuscatorContext {
    
    /**
     * 源代码
     */
    private String sourceCode;
    
    /**
     * 语言类型
     */
    private String language;
    
    /**
     * 混淆方案
     */
    private String scheme;
    
    /**
     * 其他配置项（JSON格式）
     */
    private String config;
}
