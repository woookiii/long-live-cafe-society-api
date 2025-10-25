package com.jungwook.lit_api.chat.repository;

import com.jungwook.lit_api.chat.domain.ChatParticipant;
import com.jungwook.lit_api.chat.domain.ChatRoom;
import com.jungwook.lit_api.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, UUID> {
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

    Optional<ChatParticipant> findByChatRoomAndMember(ChatRoom chatRoom, Member member);
}
