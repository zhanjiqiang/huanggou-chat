package com.huanggou.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 黄狗一号聊天机器人后端
 * 
 * @author 黄狗一号 🤬
 */
@SpringBootApplication
public class ChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
        System.out.println("🤬 黄狗一号聊天服务器启动成功！");
    }
}
