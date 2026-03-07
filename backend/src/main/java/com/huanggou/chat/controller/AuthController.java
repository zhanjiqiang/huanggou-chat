package com.huanggou.chat.controller;

import com.huanggou.chat.dto.AuthResponse;
import com.huanggou.chat.dto.LoginRequest;
import com.huanggou.chat.entity.User;
import com.huanggou.chat.service.AuthService;
import com.huanggou.chat.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final JwtUtils jwtUtils;
    
    /**
     * 注册
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.register(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 登录
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        
        if (!jwtUtils.validateToken(jwtToken)) {
            return ResponseEntity.status(401).body("Token无效或已过期");
        }
        
        Long userId = jwtUtils.getUserIdFromToken(jwtToken);
        User user = authService.getUserById(userId);
        
        return ResponseEntity.ok(new AuthResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        ));
    }
}
