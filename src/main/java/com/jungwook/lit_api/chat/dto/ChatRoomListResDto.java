package com.jungwook.lit_api.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomListResDto{
    private UUID roomId;
    private String roomName;
    private String roomDescription;
    private String category;
}
