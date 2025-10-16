package com.jungwook.lit_api.chat.dto;

public record CreateGroupRoomReqDto(
        String roomName,
        String description,
        String category
) {
}
