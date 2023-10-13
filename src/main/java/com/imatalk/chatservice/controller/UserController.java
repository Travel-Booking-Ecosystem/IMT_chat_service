package com.imatalk.chatservice.controller;

import com.imatalk.chatservice.dto.response.CommonResponse;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;
    @GetMapping("/profile")
    public ResponseEntity<CommonResponse> getProfile() {
        return userService.getProfile(getCurrentUser());
    }


    @GetMapping("/sidebar")
    public ResponseEntity<CommonResponse> getSidebar() {
        return userService.getSidebar(getCurrentUser());
    }

    //TODO: change to add friend / accept friend request


    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


}
