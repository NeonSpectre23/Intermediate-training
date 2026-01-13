package com.group38.oj.model.dto.obfuscator;

import lombok.Data;

/**
 * 代码混淆请求DTO
 */
@Data
public class ObfuscateCodeRequest {
    
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
