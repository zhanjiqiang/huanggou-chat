package com.huanggou.chat.service;

import com.huanggou.chat.dto.AuthResponse;
import com.huanggou.chat.entity.User;
import com.huanggou.chat.mapper.UserMapper;
import com.huanggou.chat.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    
    /**
     * 注册
     */
    public AuthResponse register(String username, String password) {
        // 检查用户名是否已存在
        if (userMapper.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userMapper.save(user);
        
        // 生成token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        
        return buildAuthResponse(user, token);
    }
    
    /**
     * 登录
     */
    public AuthResponse login(String username, String password) {
        // 查找用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 生成token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        
        return buildAuthResponse(user, token);
    }
    
    /**
     * 根据ID获取用户
     */
    public User getUserById(Long userId) {
        return userMapper.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    /**
     * 更新个性签名
     */
    public void updateSignature(Long userId, String signature) {
        User user = getUserById(userId);
        user.setSignature(signature);
        userMapper.save(user);
    }
    
    private AuthResponse buildAuthResponse(User user, String token) {
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null,
                user.getSignature()
        );
        
        return new AuthResponse(token, userInfo);
    }
}
