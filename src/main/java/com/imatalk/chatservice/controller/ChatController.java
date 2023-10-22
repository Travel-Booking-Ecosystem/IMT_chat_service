package com.imatalk.chatservice.controller;


import com.imatalk.chatservice.dto.request.CreateGroupConversationRequest;
import com.imatalk.chatservice.dto.request.SendMessageRequest;
import com.imatalk.chatservice.dto.response.CommonResponse;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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


    //TODO: you need to add an api to send message to user (when the conversation is not created yet)
    @PostMapping("/create-direct-conversation")
    public ResponseEntity<CommonResponse> createDirectConversation(@RequestParam String otherUserId) {
        return chatService.createDirectConversation(getCurrentUser(), otherUserId);
    }

    @PostMapping("/create-group-conversation")
    public ResponseEntity<CommonResponse> createGroupConversation(@RequestBody CreateGroupConversationRequest request) {
        return chatService.createGroupConversation(getCurrentUser(), request);
    }

    @GetMapping("/conversation-info-with-other-user/{otherUserId}")
    public ResponseEntity<CommonResponse> getConversationIdWithOtherUser(@PathVariable String otherUserId) {
        return chatService.getConversationIdWithOtherUser(getCurrentUser(), otherUserId);
    }

    // TODO: change to get send direct message
    //TODO: change to send message to conversation
    @PostMapping("/send-message")
    public ResponseEntity<CommonResponse> sendMessages(@RequestBody SendMessageRequest request) {
        return chatService.sendMessage(getCurrentUser(), request);
    }

    @GetMapping("/conversation-chat-history")
    public ResponseEntity<CommonResponse> getChatHistory(@RequestParam String conversationId,
                                                         @RequestParam(defaultValue = "-1") int messageNo) {
        return chatService.getConversationChatHistory(getCurrentUser(), conversationId, messageNo);
    }

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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




//    @PostMapping("/test")
//    public ResponseEntity<CommonResponse> test(@RequestBody SendMessageRequest request) {
//        return chatService.test(getCurrentUser(), request);
//    }
//}
}