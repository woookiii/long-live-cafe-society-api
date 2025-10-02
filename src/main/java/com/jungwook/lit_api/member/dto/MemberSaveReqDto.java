package com.jungwook.lit_api.member.dto;


import com.jungwook.lit_api.member.domain.Member;
import lombok.Builder;

@Builder
public record MemberSaveReqDto(
        String name,
        String email,
        String password
) {
    public Member toEntity(String password) {
        return Member.builder()
                .name(this.name())
                .email(this.email())
                .password(password)
                .build();
    }
}
