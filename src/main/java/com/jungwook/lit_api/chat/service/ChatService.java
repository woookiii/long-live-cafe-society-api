package com.jungwook.lit_api.chat.service;

import com.jungwook.lit_api.chat.domain.ChatMessage;
import com.jungwook.lit_api.chat.domain.ChatParticipant;
import com.jungwook.lit_api.chat.domain.ChatRoom;
import com.jungwook.lit_api.chat.dto.ChatMessageDto;
import com.jungwook.lit_api.chat.repository.ChatMessageRepository;
import com.jungwook.lit_api.chat.repository.ChatParticipantRepository;
import com.jungwook.lit_api.chat.repository.ChatRoomRepository;
import com.jungwook.lit_api.member.domain.Member;
import com.jungwook.lit_api.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    public ChatService(ChatRoomRepository chatRoomRepository, ChatParticipantRepository chatParticipantRepository, ChatMessageRepository chatMessageRepository, MemberRepository memberRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.memberRepository = memberRepository;
    }

    public void saveMessage(UUID roomId, ChatMessageDto chatMessageDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        Member sender = memberRepository.findById(chatMessageDto.getSenderId())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(chatMessageDto.getMessage())
                .build();

        chatMessageRepository.save(chatMessage);
    }

    public boolean isRoomParticipant(UUID memberId, UUID roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        for (ChatParticipant c : chatParticipants) {
            if(c.getMember().equals(member)){
                return true;
            }
        }
        return false;

    }
}
