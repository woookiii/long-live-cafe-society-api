package com.jungwook.lit_api.chat.service;

import com.jungwook.lit_api.chat.domain.Category;
import com.jungwook.lit_api.chat.domain.ChatMessage;
import com.jungwook.lit_api.chat.domain.ChatParticipant;
import com.jungwook.lit_api.chat.domain.ChatRoom;
import com.jungwook.lit_api.chat.dto.ChatMessageDto;
import com.jungwook.lit_api.chat.dto.ChatRoomListResDto;
import com.jungwook.lit_api.chat.dto.CreateGroupRoomReqDto;
import com.jungwook.lit_api.chat.repository.ChatMessageRepository;
import com.jungwook.lit_api.chat.repository.ChatParticipantRepository;
import com.jungwook.lit_api.chat.repository.ChatRoomRepository;
import com.jungwook.lit_api.image.service.FileService;
import com.jungwook.lit_api.member.domain.Member;
import com.jungwook.lit_api.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    public ChatService(FileService fileService, ChatRoomRepository chatRoomRepository, ChatParticipantRepository chatParticipantRepository, ChatMessageRepository chatMessageRepository, MemberRepository memberRepository) {
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

//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));
//
//        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
//                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));
//
//        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
//        for (ChatParticipant c : chatParticipants) {
//            if(c.getMember().equals(member)){
//                return true;
//            }
//        }
//        return false;

        return true;
    }

    public void createGroupRoom(CreateGroupRoomReqDto createGroupRoomReqDto) {
        Member sender = memberRepository.findById(UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName()))
                .orElseThrow(()->new EntityNotFoundException("Member cannot be found"));

        ChatRoom chatRoom = ChatRoom.builder()
                .name(createGroupRoomReqDto.roomName())
                .member(sender)
                .description(createGroupRoomReqDto.description())
                .category(Category.valueOf(createGroupRoomReqDto.category()))
                .isGroupChat("Y")
                .build();

        chatRoomRepository.save(chatRoom);

        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .build();

        chatParticipantRepository.save(chatParticipant);
    }

    public List<ChatRoomListResDto> getGroupChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findByIsGroupChat("Y");
        List<ChatRoomListResDto> dtos = new ArrayList<>();
        for(ChatRoom c : chatRooms) {
            ChatRoomListResDto dto = ChatRoomListResDto.builder()
                    .roomId(c.getId())
                    .roomName(c.getName())
                    .roomDescription(c.getDescription())
                    .roomCategory(c.getCategory())
                    .build();
            dtos.add(dto);
        }
        return dtos;
    }

    public void addParticipantToGroupChat(UUID roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room doesn't exist."));

        Member member = memberRepository.findById(UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName()))
                .orElseThrow(() -> new EntityNotFoundException("member doesn't exist"));

        if(chatRoom.getIsGroupChat().equals("N")) {
            throw new IllegalArgumentException("It is not group chat.");
        }

        Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndMember(chatRoom, member);
        if(!participant.isPresent()) {
            addParticipantToRoom(chatRoom, member);
        }
    }

    private void addParticipantToRoom(ChatRoom chatRoom, Member member) {
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public List<ChatMessageDto> getChatHistory(UUID roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                        .orElseThrow(() -> new EntityNotFoundException("Chat room does not exist."));

        Member member = memberRepository.findById(UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName()))
                .orElseThrow(() -> new EntityNotFoundException("Member does not exist"));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);//refactor: chatRoom.getChatParticipants()

        chatParticipants.stream()
                .filter(c -> c.getMember().equals(member))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("You are not in this chat room."));

        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);
        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();

        for(ChatMessage c : chatMessages) {
            ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                    .senderId(c.getMember().getId())
                    .message(c.getContent())
                    .senderName(c.getMember().getName())
                    .build();
            chatMessageDtos.add(chatMessageDto);
        }
        log.info("return dtos");

        return chatMessageDtos;
    }
}
