package com.group38.oj.model.dto.obfuscator;

import lombok.Data;

/**
 * 代码混淆响应DTO
 */
@Data
public class ObfuscateCodeResponse {
    
    /**
     * 混淆后的代码
     */
    private String obfuscatedCode;
}
