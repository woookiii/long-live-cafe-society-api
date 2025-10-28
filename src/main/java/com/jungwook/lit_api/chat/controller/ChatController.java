package com.jungwook.lit_api.chat.controller;

import com.jungwook.lit_api.chat.domain.Category;
import com.jungwook.lit_api.chat.dto.ChatMessageDto;
import com.jungwook.lit_api.chat.dto.ChatRoomListResDto;
import com.jungwook.lit_api.chat.dto.CreateGroupRoomReqDto;
import com.jungwook.lit_api.chat.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }


    //open group chatRoom
    @PostMapping("/room/group/create")
    public ResponseEntity<?> createGroupRoom(@RequestBody CreateGroupRoomReqDto createGroupRoomReqDto) {
        chatService.createGroupRoom(createGroupRoomReqDto);
        return ResponseEntity.ok().build();
    }

    //select group chat list
    @GetMapping("/room/group/list/{page}")
    public ResponseEntity<?> getGroupChatRooms(@PathVariable Integer page) {
        log.info("getGroupChatRooms request incoming");
        List<ChatRoomListResDto> chatRooms = chatService.getGroupChatRooms(page);
        return new ResponseEntity<>(chatRooms, HttpStatus.OK);
    }

    @GetMapping("/room/group/list/{category}/{page}")
    public ResponseEntity<?> getCategoryChatRooms(@PathVariable Category category, @PathVariable Integer page) {
        List<ChatRoomListResDto> chatRooms = chatService.getCategoryChatRooms(category, page);
        return new ResponseEntity<>(chatRooms, HttpStatus.OK);
    }

    //join group chat room
    @PostMapping("/room/group/{roomId}/join")
    public ResponseEntity<?> joinGroupChatRoom(@PathVariable UUID roomId) {
        chatService.addParticipantToGroupChat(roomId);
        return ResponseEntity.ok().build();
    }

    //select previous message
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getChatHistory(@PathVariable UUID roomId) {
        log.info("history req incoming");
        List<ChatMessageDto> chatMessageDtos = chatService.getChatHistory(roomId);
        log.info("{}",chatMessageDtos);
        return new ResponseEntity<>(chatMessageDtos, HttpStatus.OK);
    }
}
