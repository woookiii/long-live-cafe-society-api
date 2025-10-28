package com.jungwook.lit_api.image.repository;

import com.jungwook.lit_api.chat.domain.ChatRoom;
import com.jungwook.lit_api.image.domain.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MetadataRepository extends JpaRepository<Metadata, UUID> {

    Optional<Metadata> findFirstByChatRoomOrderByCreatedTimeDesc(ChatRoom chatRoom);
}
