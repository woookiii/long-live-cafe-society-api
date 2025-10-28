package com.jungwook.lit_api.chat.repository;

import com.jungwook.lit_api.chat.domain.Category;
import com.jungwook.lit_api.chat.domain.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    Page<ChatRoom> findByIsGroupChat(String isGroupChat, Pageable pageable);
    Page<ChatRoom> findByCategoryAndIsGroupChat(Category category, String isGroupChat, Pageable pageable);

}
