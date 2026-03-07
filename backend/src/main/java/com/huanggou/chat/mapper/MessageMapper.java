package com.huanggou.chat.mapper;

import com.huanggou.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 消息Mapper
 */
@Repository
public interface MessageMapper extends JpaRepository<Message, Long> {
    List<Message> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
