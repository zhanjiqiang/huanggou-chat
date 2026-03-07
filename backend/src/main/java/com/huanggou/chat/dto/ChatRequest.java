package com.huanggou.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 聊天请求DTO
 */
@Data
public class ChatRequest {
    @NotBlank(message = "会话ID不能为空")
    private Long sessionId;
    
    @NotBlank(message = "消息内容不能为空")
    private String message;
    
    private String model = "zai/glm-4.7";
}
