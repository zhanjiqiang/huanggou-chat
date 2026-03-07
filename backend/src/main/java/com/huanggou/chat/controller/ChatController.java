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
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/api/chat")
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
     * 流式发送消息（SSE）
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendStream(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader("Authorization") String token) {
        
        Long userId = getUserIdFromToken(token);
        return chatService.sendMessageStream(
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
        
        // 这里可以添加图片识别逻辑
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
