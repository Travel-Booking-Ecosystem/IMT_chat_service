package com.imatalk.chatservice.controller;


import com.imatalk.chatservice.dto.request.SendMessageRequest;
import com.imatalk.chatservice.dto.response.CommonResponse;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/create-direct-conversation")
    public ResponseEntity<CommonResponse> createDirectConversation(@RequestParam String otherUserId) {
        return chatService.createDirectConversation(getCurrentUser(), otherUserId);
    }

    @PostMapping("/send-direct-message")
    public ResponseEntity<CommonResponse> sendDirectMessage(@RequestBody SendMessageRequest request) {
        return chatService.sendDirectMessage(getCurrentUser(), request);
    }

    @GetMapping("/get-direct-conversation-messages")
    public ResponseEntity<CommonResponse> getDirectConversationMessages(@RequestParam String conversationId,
                                                                        @RequestParam(defaultValue = "-1") int messageNo) {
        // if page is not specified, return the latest 100 messages
        return chatService.getDirectConversationMessages(getCurrentUser(), conversationId, messageNo);
    }

    @GetMapping("/profile")
    public ResponseEntity<CommonResponse> getProfile() {
        return chatService.getProfile(getCurrentUser());
    }

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
