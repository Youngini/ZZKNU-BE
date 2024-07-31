package com.zzknu.back_end.domain.user.controller;

import com.zzknu.back_end.domain.user.dto.LoginRequestDto;
import com.zzknu.back_end.domain.user.dto.UserRequestDto;
import com.zzknu.back_end.domain.user.entity.User;
import com.zzknu.back_end.domain.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<Long> login(@RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    // 회원 가입
    @PostMapping("/join")
    public ResponseEntity<User> createUser(@RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.ok(authService.createUser(userRequestDto));
    }
}
