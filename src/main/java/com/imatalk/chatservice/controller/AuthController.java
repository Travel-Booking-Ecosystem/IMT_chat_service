package com.imatalk.chatservice.controller;


import com.imatalk.chatservice.dto.request.LoginRequest;
import com.imatalk.chatservice.dto.request.RegistrationRequest;
import com.imatalk.chatservice.dto.response.CommonResponse;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<CommonResponse> register(@RequestBody @Validated RegistrationRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse> register(@RequestBody  @Validated LoginRequest request) {
        return authService.login(request);
    }

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
