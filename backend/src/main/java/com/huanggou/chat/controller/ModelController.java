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
    
    // 可用模型列表（智谱 AI 原生 API）
    private static final List<Map<String, Object>> AVAILABLE_MODELS = Arrays.asList(
        Map.of("id", "glm-4.7", "name", "GLM-4.7 (推荐)", "recommended", true),
        Map.of("id", "glm-5", "name", "GLM-5 (最强)", "recommended", false),
        Map.of("id", "glm-4.6", "name", "GLM-4.6", "recommended", false),
        Map.of("id", "glm-4.5-air", "name", "GLM-4.5 Air (快速)", "recommended", false),
        Map.of("id", "glm-4.5", "name", "GLM-4.5", "recommended", false)
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
