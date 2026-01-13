package com.group38.oj.obfuscator;

import com.group38.oj.common.ErrorCode;
import com.group38.oj.exception.BusinessException;
import com.group38.oj.obfuscator.strategy.ObfuscatorContext;
import com.group38.oj.obfuscator.strategy.ObfuscatorStrategy;
import org.springframework.stereotype.Service;

// 混淆器管理器，用于协调混淆器的使用
@Service
public class ObfuscatorManager {
    
    private final ObfuscatorFactory obfuscatorFactory;
    
    public ObfuscatorManager(ObfuscatorFactory obfuscatorFactory) {
        this.obfuscatorFactory = obfuscatorFactory;
    }
    
    /**
     * 执行代码混淆
     * @param obfuscatorContext 混淆上下文
     * @return 混淆后的代码
     */
    public String executeObfuscation(ObfuscatorContext obfuscatorContext) {
        String language = obfuscatorContext.getLanguage();
        String scheme = obfuscatorContext.getScheme();
        
        // 获取对应的混淆策略
        ObfuscatorStrategy obfuscatorStrategy = obfuscatorFactory.getObfuscatorStrategy(language, scheme);
        
        if (obfuscatorStrategy == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, 
                                      "Unsupported obfuscation strategy: " + language + ":" + scheme);
        }
        
        // 执行混淆
        return obfuscatorStrategy.obfuscate(obfuscatorContext);
    }
}
