package com.imatalk.chatservice.controller;


import com.imatalk.chatservice.dto.request.CreateGroupConversationRequest;
import com.imatalk.chatservice.dto.request.ReactMessageRequest;
import com.imatalk.chatservice.dto.request.SendMessageRequest;
import com.imatalk.chatservice.dto.request.UpdateConversationSettingRequest;
import com.imatalk.chatservice.dto.response.CommonResponse;
import com.imatalk.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
//TODO: use API Gateway and Load Balancer
// TODO: you should have storing BLOB, Encryption for messages
//@CrossOrigin(origins = "http://localhost:3000")
//TODO: please add logging to all the methods (AOP)

// TODO: add logging
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/chat-user/{id}")
    public ResponseEntity<CommonResponse> getChatUserById(@PathVariable String id) {
        return chatService.test(id);
    }


    @GetMapping("/health")
    public ResponseEntity<CommonResponse> health() {
        Map<String, String> map = Map.of(
                "service", "chat-service",
                "status", "OK",
                "time", LocalDateTime.now().toString());
        return ResponseEntity.ok(CommonResponse.success("Health check",map));
    }

    //TODO: you need to add an api to send message to user (when the conversation is not created yet)
//    @PostMapping("/create-direct-conversation")
//    public ResponseEntity<CommonResponse> createDirectConversation(@RequestHeader String currentUserId, @RequestParam String otherUserId) {
//        return chatService.createDirectConversation(currentUserId, otherUserId);
//    }

    @PostMapping("/create-group-conversation")
    public ResponseEntity<CommonResponse> createGroupConversation(@RequestHeader String currentUserId, @RequestBody CreateGroupConversationRequest request) {
        return chatService.createGroupConversation(currentUserId, request);
    }

    @GetMapping("/find-conversation-with/{otherUserId}")
    public ResponseEntity<CommonResponse> getConversationIdWithOtherUser(@RequestHeader String currentUserId, @PathVariable String otherUserId) {
        return chatService.getConversationIdWithOtherUser(currentUserId, otherUserId);
    }
    @GetMapping("/conversation-list")
    public ResponseEntity<CommonResponse> getSidebar(@RequestHeader String currentUserId) {
        return chatService.getConversationList(currentUserId);
    }

    @PostMapping("/update-conversation-setting")
    public ResponseEntity<CommonResponse> updateConversationSetting(@RequestHeader String currentUserId, @RequestBody UpdateConversationSettingRequest request) {
        return chatService.updateConversationSetting(currentUserId, request);
    }

    @PostMapping("/send-message")
    public ResponseEntity<CommonResponse> sendMessages(@RequestHeader String currentUserId, @RequestBody SendMessageRequest request) {
        return chatService.sendMessage(currentUserId, request);
    }

    @GetMapping("/conversation-chat-history")
    public ResponseEntity<CommonResponse> getChatHistory(@RequestHeader String currentUserId, @RequestParam String conversationId,
                                                         @RequestParam(defaultValue = "-1") int messageNo) {
        return chatService.getConversationDetails(currentUserId, conversationId, messageNo);
    }

//    @MessageMapping("/message")
//    @SendTo("/chatroom/public")
//    public String receiveMessage(@Payload String message){
//        return message;
//    }
//
    @MessageMapping("/message-received")
    public String recString(@Payload String message){
//        simpMessagingTemplate.convertAndSendToUser(message.getReceiverName(),"/private",message);
        System.out.println(message.toString());
        return message;
    }


    @MessageMapping("/message-seen")
    public String recStringSeen(@Payload String message){
        System.out.println("seen");
        System.out.println(message);
        return message;
    }

    @PostMapping("/react-message")
    public ResponseEntity<CommonResponse> reactMessage(@RequestHeader String currentUserId, @RequestBody ReactMessageRequest request) {
        return chatService.reactMessage(currentUserId, request);
    }

//    @PostMapping("/test")
//    public ResponseEntity<CommonResponse> test(@RequestBody SendMessageRequest request) {
//        return chatService.test(getCurrentUser(), request);
//    }
//}
}