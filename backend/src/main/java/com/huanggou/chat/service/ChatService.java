package com.huanggou.chat.service;

import com.huanggou.chat.dto.ChatResponse;
import com.huanggou.chat.entity.Message;
import com.huanggou.chat.entity.Session;
import com.huanggou.chat.mapper.MessageMapper;
import com.huanggou.chat.mapper.SessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * 聊天服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final MessageMapper messageMapper;
    private final SessionMapper sessionMapper;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${openclaw.gateway.base-url}")
    private String openclawBaseUrl;
    
    /**
     * 发送消息（非流式）
     */
    public ChatResponse sendMessage(Long sessionId, String content, String model) {
        // 保存用户消息
        Message userMessage = new Message();
        userMessage.setSessionId(sessionId);
        userMessage.setRole("user");
        userMessage.setContent(content);
        userMessage.setModel(model);
        messageMapper.save(userMessage);
        
        // 调用OpenClaw API
        // 这里简化处理，实际需要调用OpenClaw的sessions_send接口
        String reply = "这是AI的回复：收到你的消息 - " + content;
        
        // 保存AI回复
        Message assistantMessage = new Message();
        assistantMessage.setSessionId(sessionId);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(reply);
        assistantMessage.setModel(model);
        messageMapper.save(assistantMessage);
        
        return ChatResponse.of(reply, model);
    }
    
    /**
     * 流式发送消息
     */
    public Flux<String> sendMessageStream(Long sessionId, String content, String model) {
        // 保存用户消息
        Message userMessage = new Message();
        userMessage.setSessionId(sessionId);
        userMessage.setRole("user");
        userMessage.setContent(content);
        userMessage.setModel(model);
        messageMapper.save(userMessage);
        
        // 获取会话的sessionKey
        Session session = sessionMapper.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));
        
        // 调用OpenClaw流式API
        // 这里返回一个模拟的流式响应
        return Flux.just(
                "这是", "流式", "回复", "：", content,
                "\n\n", "🤬", " 黄狗一号", "为您服务！"
        );
    }
}
