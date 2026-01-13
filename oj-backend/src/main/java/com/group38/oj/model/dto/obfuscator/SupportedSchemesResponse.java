package com.group38.oj.model.dto.obfuscator;

import lombok.Data;

import java.util.Map;
import java.util.List;

/**
 * 支持的混淆方案响应DTO
 */
@Data
public class SupportedSchemesResponse {
    
    /**
     * 按语言分组的支持方案列表
     * key: 语言类型
     * value: 该语言支持的方案列表
     */
    private Map<String, List<String>> schemesByLanguage;
}
