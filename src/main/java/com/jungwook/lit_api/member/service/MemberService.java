package com.jungwook.lit_api.member.service;

import com.jungwook.lit_api.member.domain.Member;
import com.jungwook.lit_api.member.dto.MemberSaveReqDto;
import com.jungwook.lit_api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    public Member create(MemberSaveReqDto memberSaveReqDto) {
        Optional<Member> optionalMember = memberRepository.findByEmail(memberSaveReqDto.email());
        if(optionalMember.isPresent()) {
            throw new IllegalArgumentException("This email already exist.");
        }

        String password = passwordEncoder.encode(memberSaveReqDto.password());
        Member member = memberRepository.save(memberSaveReqDto.toEntity(password));
    }
}
