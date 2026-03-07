package com.huanggou.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 模型控制器
 */
@RestController
@RequestMapping("/api/models")
public class ModelController {
    
    // 可用模型列表（OpenRouter 格式）
    private static final List<Map<String, Object>> AVAILABLE_MODELS = Arrays.asList(
        Map.of("id", "liquid/lfm-2.5-1.2b-instruct:free", "name", "LFM 2.5 1.2B (免费)", "recommended", true),
        Map.of("id", "z-ai/glm-4.7", "name", "GLM-4.7", "recommended", false),
        Map.of("id", "z-ai/glm-5", "name", "GLM-5 (最强)", "recommended", false),
        Map.of("id", "meta-llama/llama-3.3-70b-instruct:free", "name", "Llama 3.3 70B (免费)", "recommended", false)
    );
    
    /**
     * 获取所有可用模型
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableModels() {
        return ResponseEntity.ok(Map.of("models", AVAILABLE_MODELS));
    }
    
    /**
     * 获取用户模型列表
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyModels() {
        // 简单返回所有可用模型
        return ResponseEntity.ok(Map.of("models", AVAILABLE_MODELS));
    }
}
