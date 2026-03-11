package com.huanggou.chat.controller;

import com.huanggou.chat.entity.Session;
import com.huanggou.chat.service.SessionService;
import com.huanggou.chat.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 会话控制器
 */
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {
    
    private final SessionService sessionService;
    private final JwtUtils jwtUtils;
    
    /**
     * 获取用户的会话列表
     */
    @GetMapping
    public ResponseEntity<?> getSessions(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        List<Session> sessions = sessionService.getUserSessions(userId);
        return ResponseEntity.ok(Map.of("sessions", sessions));
    }
    
    /**
     * 创建新会话
     */
    @PostMapping
    public ResponseEntity<?> createSession(
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader("Authorization") String token) {
        
        Long userId = getUserIdFromToken(token);
        String title = body != null ? body.get("title") : "新对话";
        String model = body != null ? body.get("model") : "zai/glm-4.7";
        
        Session session = sessionService.createSession(userId, title);
        return ResponseEntity.ok(session);
    }
    
    /**
     * 获取会话详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSession(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        
        Long userId = getUserIdFromToken(token);
        Session session = sessionService.getSession(userId, id);
        return ResponseEntity.ok(Map.of("session", session));
    }
    
    /**
     * 删除会话
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSession(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        
        Long userId = getUserIdFromToken(token);
        sessionService.deleteSession(userId, id);
        return ResponseEntity.ok(Map.of("success", true));
    }
    
    private Long getUserIdFromToken(String token) {
        String jwtToken = token.replace("Bearer ", "");
        return jwtUtils.getUserIdFromToken(jwtToken);
    }
}
