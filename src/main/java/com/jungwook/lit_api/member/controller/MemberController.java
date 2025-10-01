package com.jungwook.lit_api.member.controller;

import com.jungwook.lit_api.common.auth.JwtTokenProvider;
import com.jungwook.lit_api.member.domain.Member;
import com.jungwook.lit_api.member.dto.MemberLoginReqDto;
import com.jungwook.lit_api.member.dto.MemberSaveReqDto;
import com.jungwook.lit_api.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Qualifier("rtdb")
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider, RedisTemplate<String, Object> redisTemplate) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/create")
    public ResponseEntity<?> memberCreate(@RequestBody MemberSaveReqDto memberSaveReqDto){
        UUID memberId = memberService.create(memberSaveReqDto);
        return new ResponseEntity<>(memberId, HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginReqDto dto, HttpServletResponse response){
        Member member = memberService.login(dto);
        String token = jwtTokenProvider.createToken(member.getId().toString(), member.getRole().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId().toString(), member.getRole().toString());

        redisTemplate.opsForValue()//operation for value like String or Objects
                .set(member.getId().toString(), refreshToken, 200, TimeUnit.DAYS); //200 days ttl

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(30 * 24 * 60 * 60) // 30 days
                .build();
        response.addHeader("Set-Cookie", refreshCookie.toString());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("name", member.getName());
        loginInfo.put("token", token);

        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> generateNewAt(@CookieValue(required = false) String refreshToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKeyRt)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        Object rt = redisTemplate.opsForValue().get(claims.getSubject());
        if(rt == null || !rt.toString().equals(refreshToken)){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        String token = jwtTokenProvider.createToken(claims.getSubject(), claims.get("role").toString());//set subject as email when refreshing

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("token", token);

        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    @PostMapping("/doLogout")
    public ResponseEntity<?> doLogout(@CookieValue(required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken != null) {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKeyRt)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
            redisTemplate.delete(claims.getSubject());
        }

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", deleteCookie.toString());

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
