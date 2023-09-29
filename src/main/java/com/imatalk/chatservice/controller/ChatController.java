package com.imatalk.chatservice.controller;


import com.imatalk.chatservice.dto.request.SendMessageRequest;
import com.imatalk.chatservice.dto.response.CommonResponse;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:3000")
//TODO: please add logging to all the methods (AOP)
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/create-direct-conversation")
    public ResponseEntity<CommonResponse> createDirectConversation(@RequestParam String otherUserId) {
        return chatService.createDirectConversation(getCurrentUser(), otherUserId);
    }

    @PostMapping("/send-direct-message")
    public ResponseEntity<CommonResponse> sendDirectMessage(@RequestBody SendMessageRequest request) {
        System.out.println("Request: " + request.toString());
        return chatService.sendDirectMessage(getCurrentUser(), request);
    }

    @GetMapping("/get-direct-conversation-messages")
    public ResponseEntity<CommonResponse> getDirectConversationMessages(@RequestParam String conversationId,
                                                                        @RequestParam(defaultValue = "-1") int messageNo) {
        // if page is not specified, return the latest 100 messages
        return chatService.getDirectConversationDetail(getCurrentUser(), conversationId, messageNo);
    }

    @GetMapping("/profile")
    public ResponseEntity<CommonResponse> getProfile() {
        return chatService.getProfile(getCurrentUser());
    }

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @MessageMapping("/message")
    @SendTo("/chatroom/public")
    public String receiveMessage(@Payload String message){
        return message;
    }

    @MessageMapping("/private-message")
    public String recString(@Payload String message){
//        simpMessagingTemplate.convertAndSendToUser(message.getReceiverName(),"/private",message);
        System.out.println(message.toString());
        return message;
    }


    @PostMapping("/test")
    public ResponseEntity<CommonResponse> test(@RequestBody SendMessageRequest request) {
        return chatService.test(getCurrentUser(), request);
    }
}
