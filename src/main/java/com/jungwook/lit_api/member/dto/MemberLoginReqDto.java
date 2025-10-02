package com.jungwook.lit_api.member.dto;


public record MemberLoginReqDto(
        String email,
        String password
) {
}
