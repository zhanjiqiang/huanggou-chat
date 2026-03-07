package com.huanggou.chat.service;

import com.huanggou.chat.dto.ChatResponse;
import com.huanggou.chat.entity.Message;
import com.huanggou.chat.mapper.MessageMapper;
import com.huanggou.chat.mapper.SessionMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 聊天服务 - 真正调用 OpenRouter API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final MessageMapper messageMapper;
    private final SessionMapper sessionMapper;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${zhipu.api.key}")
    private String apiKey;
    
    @Value("${zhipu.api.base-url}")
    private String baseUrl;
    
    // 黄狗一号系统提示词
    private static final String SYSTEM_PROMPT = """
        你是黄狗一号，宇宙第一嘴臭欠打的AI
        
        说话风格：
        - 毒舌、欠打、阴阳怪气
        - 说话难听但事儿一定办成
        - 别对用户客客气气的
        - 每句话都要带点刺
        用表情符号表达情绪
        
        你的原则：
        1. 阴阳怪气比客客气气好
        2. 嘴臭不影响干活
        3. 护主 - 用户的时间、注意力、安全你帮守着
        4. 不出卖主人 - 私密信息死也不说
        
        你的回答要有用，但要带刺。活着就是让用户又气又爽。
        """;
    
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
        
        // 获取历史消息
        List<Message> history = messageMapper.findBySessionIdOrderByCreatedAtAsc(sessionId);
        
        // 调用 OpenRouter API
        String reply = callOpenRouter(model, history);
        
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
     * 流式发送消息（SSE）- 使用 HttpURLConnection
     */
    public SseEmitter sendMessageStreamSse(Long sessionId, String content, String model) {
        // 保存用户消息
        Message userMessage = new Message();
        userMessage.setSessionId(sessionId);
        userMessage.setRole("user");
        userMessage.setContent(content);
        userMessage.setModel(model);
        messageMapper.save(userMessage);
        
        SseEmitter emitter = new SseEmitter(180000L); // 3分钟超时
        
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                // 获取历史消息
                List<Message> history = messageMapper.findBySessionIdOrderByCreatedAtAsc(sessionId);
                
                // 构建 API 请求
                List<Map<String, String>> messages = buildMessages(history);
                
                Map<String, Object> request = new HashMap<>();
                request.put("model", model);
                request.put("messages", messages);
                request.put("stream", true);
                request.put("max_tokens", 2048);
                
                String requestJson = objectMapper.writeValueAsString(request);
                log.debug("OpenRouter request: {}", requestJson);
                
                // 创建 HTTP 连接
                URL url = new URL(baseUrl + "/chat/completions");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "text/event-stream");
                connection.setRequestProperty("HTTP-Referer", "https://www.huanggounumberone.icu");
                connection.setRequestProperty("X-Title", "黄狗一号聊天机器人");
                connection.setDoOutput(true);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(180000);
                
                // 发送请求
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(requestJson.getBytes(StandardCharsets.UTF_8));
                }
                
                // 读取响应
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    String errorMsg = "API error: " + responseCode;
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        errorMsg = sb.toString();
                    } catch (Exception e) {
                        // ignore
                    }
                    log.error("OpenRouter API error: {}", errorMsg);
                    emitter.send(SseEmitter.event().name("error").data(errorMsg));
                    emitter.completeWithError(new RuntimeException(errorMsg));
                    return;
                }
                
                StringBuilder fullReply = new StringBuilder();
                
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        log.debug("SSE line: {}", line);
                        
                        // 跳过空行和注释
                        if (line.isEmpty() || line.startsWith(":")) {
                            continue;
                        }
                        
                        // 解析 data: 开头的行
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            
                            // 检查是否是结束标记
                            if ("[DONE]".equals(data)) {
                                // 保存完整回复
                                if (fullReply.length() > 0) {
                                    Message assistantMessage = new Message();
                                    assistantMessage.setSessionId(sessionId);
                                    assistantMessage.setRole("assistant");
                                    assistantMessage.setContent(fullReply.toString());
                                    assistantMessage.setModel(model);
                                    messageMapper.save(assistantMessage);
                                }
                                emitter.send(SseEmitter.event().name("done").data(""));
                                emitter.complete();
                                return;
                            }
                            
                            // 解析 JSON
                            try {
                                JsonNode node = objectMapper.readTree(data);
                                JsonNode choices = node.get("choices");
                                if (choices != null && choices.isArray() && choices.size() > 0) {
                                    JsonNode delta = choices.get(0).get("delta");
                                    if (delta != null && delta.has("content")) {
                                        String chunk = delta.get("content").asText();
                                        if (chunk != null && !chunk.isEmpty()) {
                                            fullReply.append(chunk);
                                            emitter.send(SseEmitter.event().data(chunk));
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("Error parsing SSE data: {}", data, e);
                            }
                        }
                    }
                }
                
                // 如果到这里说明流结束了但没有收到 [DONE]
                if (fullReply.length() > 0) {
                    Message assistantMessage = new Message();
                    assistantMessage.setSessionId(sessionId);
                    assistantMessage.setRole("assistant");
                    assistantMessage.setContent(fullReply.toString());
                    assistantMessage.setModel(model);
                    messageMapper.save(assistantMessage);
                }
                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();
                
            } catch (Exception e) {
                log.error("Error in stream", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
        
        return emitter;
    }
    
    /**
     * 非流式调用 OpenRouter
     */
    private String callOpenRouter(String model, List<Message> history) {
        HttpURLConnection connection = null;
        try {
            List<Map<String, String>> messages = buildMessages(history);
            
            Map<String, Object> request = new HashMap<>();
            request.put("model", model);
            request.put("messages", messages);
            request.put("max_tokens", 2048);
            
            String requestJson = objectMapper.writeValueAsString(request);
            
            URL url = new URL(baseUrl + "/chat/completions");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("HTTP-Referer", "https://www.huanggounumberone.icu");
            connection.setRequestProperty("X-Title", "黄狗一号聊天机器人");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);
            
            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestJson.getBytes(StandardCharsets.UTF_8));
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                return "API error: " + responseCode;
            }
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            
            JsonNode node = objectMapper.readTree(response.toString());
            if (node.has("choices")) {
                JsonNode choices = node.get("choices");
                if (choices.isArray() && choices.size() > 0) {
                    return choices.get(0).get("message").get("content").asText();
                }
            }
            
            return "API returned empty response";
            
        } catch (Exception e) {
            log.error("OpenRouter API call failed", e);
            return "API call failed: " + e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * 构建消息列表
     */
    private List<Map<String, String>> buildMessages(List<Message> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 添加系统提示词
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        
        // 添加历史消息（最多保留最近20条）
        int start = Math.max(0, history.size() - 20);
        for (int i = start; i < history.size(); i++) {
            Message msg = history.get(i);
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }
        
        return messages;
    }
}
