package com.huanggou.chat.service;

import com.huanggou.chat.entity.Session;
import com.huanggou.chat.mapper.MessageMapper;
import com.huanggou.chat.mapper.SessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

/**
 * 会话服务
 */
@Service
@RequiredArgsConstructor
public class SessionService {
    
    private final SessionMapper sessionMapper;
    private final MessageMapper messageMapper;
    
    /**
     * 获取用户的会话列表
     */
    public List<Session> getUserSessions(Long userId) {
        return sessionMapper.findByUserIdOrderByUpdatedAtDesc(userId);
    }
    
    /**
     * 创建新会话
     */
    public Session createSession(Long userId, String title) {
        Session session = new Session();
        session.setUserId(userId);
        session.setTitle(title != null ? title : "新对话");
        session.setSessionKey(UUID.randomUUID().toString());
        return sessionMapper.save(session);
    }
    
    /**
     * 删除会话
     */
    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        Session session = sessionMapper.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));
        
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此会话");
        }
        
        // 先删除消息
        messageMapper.deleteAll(messageMapper.findBySessionIdOrderByCreatedAtAsc(sessionId));
        
        // 再删除会话
        sessionMapper.delete(session);
    }
    
    /**
     * 获取会话详情
     */
    public Session getSession(Long userId, Long sessionId) {
        Session session = sessionMapper.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));
        
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此会话");
        }
        
        return session;
    }
}
