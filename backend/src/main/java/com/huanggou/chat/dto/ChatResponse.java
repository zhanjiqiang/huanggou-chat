package com.huanggou.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String reply;
    private String model;
    
    public static ChatResponse of(String reply) {
        return new ChatResponse(reply, null);
    }
    
    public static ChatResponse of(String reply, String model) {
        return new ChatResponse(reply, model);
    }
}
