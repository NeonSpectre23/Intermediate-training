package com.group38.oj.controller;

import com.group38.oj.common.BaseResponse;
import com.group38.oj.common.ErrorCode;
import com.group38.oj.common.ResultUtils;
import com.group38.oj.model.dto.obfuscator.ObfuscateCodeRequest;
import com.group38.oj.model.dto.obfuscator.ObfuscateCodeResponse;
import com.group38.oj.model.dto.obfuscator.SupportedSchemesResponse;
import com.group38.oj.obfuscator.ObfuscatorFactory;
import com.group38.oj.obfuscator.ObfuscatorManager;
import com.group38.oj.obfuscator.strategy.ObfuscatorContext;
import com.group38.oj.obfuscator.strategy.ObfuscatorStrategy;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// 代码混淆控制器
@RestController
@RequestMapping("/obfuscator")
public class ObfuscatorController {
    
    private final ObfuscatorManager obfuscatorManager;
    private final ObfuscatorFactory obfuscatorFactory;
    
    public ObfuscatorController(ObfuscatorManager obfuscatorManager, ObfuscatorFactory obfuscatorFactory) {
        this.obfuscatorManager = obfuscatorManager;
        this.obfuscatorFactory = obfuscatorFactory;
    }
    
    /**
     * 获取支持的混淆策略
     * @return 支持的混淆策略列表
     */
    @GetMapping("/schemes")
    public BaseResponse<SupportedSchemesResponse> getSupportedSchemes() {
        Map<String, ObfuscatorStrategy> allStrategies = obfuscatorFactory.getAllObfuscatorStrategies();
        
        // 按语言分组，只保留支持的语言：java, cpp, c, python, go
        Map<String, List<String>> schemesByLanguage = new HashMap<>();
        // 定义支持的语言列表
        List<String> supportedLanguages = Arrays.asList("java", "cpp", "c", "python", "go");
        
        for (ObfuscatorStrategy strategy : allStrategies.values()) {
            String language = strategy.getSupportedLanguage();
            // 只处理支持的语言
            if (supportedLanguages.contains(language)) {
                String scheme = strategy.getSupportedScheme();
                schemesByLanguage.computeIfAbsent(language, k -> new java.util.ArrayList<>())
                               .add(scheme);
            }
        }
        
        SupportedSchemesResponse response = new SupportedSchemesResponse();
        response.setSchemesByLanguage(schemesByLanguage);
        
        return ResultUtils.success(response);
    }
    
    /**
     * 执行代码混淆
     * @param request 混淆请求参数
     * @return 混淆结果
     */
    @PostMapping("/obfuscate")
    public BaseResponse<ObfuscateCodeResponse> obfuscateCode(@RequestBody ObfuscateCodeRequest request) {
        // 定义支持的语言列表
        List<String> supportedLanguages = Arrays.asList("java", "cpp", "c", "python", "go");
        
        // 验证语言是否支持
        String language = request.getLanguage();
        if (!supportedLanguages.contains(language)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "不支持的语言，仅支持java, cpp, c, python, go");
        }
        
        // 构建混淆上下文
        ObfuscatorContext context = new ObfuscatorContext();
        context.setSourceCode(request.getSourceCode());
        context.setLanguage(request.getLanguage());
        context.setScheme(request.getScheme());
        context.setConfig(request.getConfig());
        
        // 执行混淆
        String obfuscatedCode = obfuscatorManager.executeObfuscation(context);
        
        // 构建响应
        ObfuscateCodeResponse response = new ObfuscateCodeResponse();
        response.setObfuscatedCode(obfuscatedCode);
        
        return ResultUtils.success(response);
    }
}
