package com.huanggou.chat.mapper;

import com.huanggou.chat.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 会话Mapper
 */
@Repository
public interface SessionMapper extends JpaRepository<Session, Long> {
    List<Session> findByUserIdOrderByUpdatedAtDesc(Long userId);
    List<Session> findByUserId(Long userId);
}
