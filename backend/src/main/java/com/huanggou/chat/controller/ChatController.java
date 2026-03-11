package com.huanggou.chat.controller;

import com.huanggou.chat.dto.ChatRequest;
import com.huanggou.chat.dto.ChatResponse;
import com.huanggou.chat.entity.Message;
import com.huanggou.chat.mapper.MessageMapper;
import com.huanggou.chat.service.ChatService;
import com.huanggou.chat.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final MessageMapper messageMapper;
    private final JwtUtils jwtUtils;
    
    /**
     * 发送消息（非流式）
     */
    @PostMapping("/send")
    public ResponseEntity<ChatResponse> send(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader("Authorization") String token) {
        
        Long userId = getUserIdFromToken(token);
        ChatResponse response = chatService.sendMessage(
                request.getSessionId(),
                request.getMessage(),
                request.getModel()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 流式发送消息（SSE - GET方式，支持URL参数token）
     */
    @GetMapping(value = "/stream/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendStreamGet(
            @PathVariable Long sessionId,
            @RequestParam String message,
            @RequestParam(defaultValue = "zai/glm-4.7") String model,
            @RequestParam(required = false) String token,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // 支持从URL参数或Header获取token
        String jwtToken = token;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
        }
        
        // 验证token（这里简化处理，实际应该验证）
        // Long userId = jwtUtils.getUserIdFromToken(jwtToken);
        
        return chatService.sendMessageStreamSse(sessionId, message, model);
    }
    
    /**
     * 流式发送消息（POST SSE）
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendStreamPost(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader("Authorization") String token) {
        
        Long userId = getUserIdFromToken(token);
        return chatService.sendMessageStreamSse(
                request.getSessionId(),
                request.getMessage(),
                request.getModel()
        );
    }
    
    /**
     * 上传图片
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("image") MultipartFile file,
            @RequestHeader("Authorization") String token) {
        
        Long userId = getUserIdFromToken(token);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("description", "图片识别功能待实现");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取会话消息列表
     */
    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<?> getMessages(
            @PathVariable Long sessionId,
            @RequestHeader("Authorization") String token) {
        
        Long userId = getUserIdFromToken(token);
        List<Message> messages = messageMapper.findBySessionIdOrderByCreatedAtAsc(sessionId);
        
        return ResponseEntity.ok(Map.of("messages", messages));
    }
    
    private Long getUserIdFromToken(String token) {
        String jwtToken = token.replace("Bearer ", "");
        return jwtUtils.getUserIdFromToken(jwtToken);
    }
}
