package com.imatalk.chatservice.controller;

import com.imatalk.chatservice.dto.response.CommonResponse;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;
    @GetMapping("/profile")
    public ResponseEntity<CommonResponse> getProfile() {
        return userService.getProfile(getCurrentUser());
    }


    // TODO: get conversation info list
    @GetMapping("/conversation-list")
    public ResponseEntity<CommonResponse> getSidebar() {
        return userService.getConversationList(getCurrentUser());
    }


    @GetMapping("/friend-request")
    public ResponseEntity<CommonResponse> getFriendRequest() {
        return userService.getFriendRequest(getCurrentUser());
    }

    //TODO: change to add friend / accept friend request
    @GetMapping("/friends")
    public ResponseEntity<CommonResponse> getFriends() {
        return userService.getFriends(getCurrentUser());
    }

    @PostMapping("/add-friend")
    public ResponseEntity<CommonResponse> addFriend(@RequestParam String otherUserId) {
        return userService.addFriend(getCurrentUser(), otherUserId);
    }

    @PostMapping("/accept-friend")
    public ResponseEntity<CommonResponse> acceptFriend(@RequestParam String requestId) {
        return userService.acceptFriend(getCurrentUser(), requestId);
    }

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


}
