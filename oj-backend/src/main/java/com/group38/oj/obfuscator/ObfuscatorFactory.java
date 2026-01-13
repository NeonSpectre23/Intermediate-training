package com.group38.oj.obfuscator;

import com.group38.oj.obfuscator.strategy.ObfuscatorStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

// 混淆器工厂，用于创建具体的混淆策略实例
@Component
public class ObfuscatorFactory {
    
    // 存储所有混淆策略的映射：language:scheme -> strategy
    private final Map<String, ObfuscatorStrategy> obfuscatorStrategyMap = new HashMap<>();
    
    /**
     * 构造函数，自动注入所有实现了ObfuscatorStrategy接口的bean
     * @param strategies 所有实现了ObfuscatorStrategy接口的bean
     */
    @Autowired
    public ObfuscatorFactory(Map<String, ObfuscatorStrategy> strategies) {
        // 初始化混淆器策略映射
        for (ObfuscatorStrategy strategy : strategies.values()) {
            String key = generateKey(strategy.getSupportedLanguage(), strategy.getSupportedScheme());
            obfuscatorStrategyMap.put(key, strategy);
        }
    }
    
    /**
     * 根据语言和方案获取混淆策略
     * @param language 语言类型
     * @param scheme 混淆方案
     * @return 混淆策略实例
     */
    public ObfuscatorStrategy getObfuscatorStrategy(String language, String scheme) {
        String key = generateKey(language, scheme);
        return obfuscatorStrategyMap.get(key);
    }
    
    /**
     * 获取所有支持的混淆策略
     * @return 混淆策略映射
     */
    public Map<String, ObfuscatorStrategy> getAllObfuscatorStrategies() {
        return new HashMap<>(obfuscatorStrategyMap);
    }
    
    /**
     * 生成映射键
     * @param language 语言类型
     * @param scheme 混淆方案
     * @return 映射键
     */
    private String generateKey(String language, String scheme) {
        return language.toLowerCase() + ":" + scheme.toLowerCase();
    }
    
    /**
     * 添加混淆策略
     * @param strategy 混淆策略实例
     */
    public void addObfuscatorStrategy(ObfuscatorStrategy strategy) {
        String key = generateKey(strategy.getSupportedLanguage(), strategy.getSupportedScheme());
        obfuscatorStrategyMap.put(key, strategy);
    }
    
    /**
     * 移除混淆策略
     * @param language 语言类型
     * @param scheme 混淆方案
     */
    public void removeObfuscatorStrategy(String language, String scheme) {
        String key = generateKey(language, scheme);
        obfuscatorStrategyMap.remove(key);
    }
}
