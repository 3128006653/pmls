package com.example1.controller;

import com.example1.common.Result;
import com.example1.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest req) {
        return Result.ok(authService.login(req.getUsername(), req.getPassword()));
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
