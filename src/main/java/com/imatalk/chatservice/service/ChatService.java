package com.imatalk.chatservice.service;

import com.imatalk.chatservice.dto.request.CreateGroupConversationRequest;
import com.imatalk.chatservice.dto.request.SendMessageRequest;
import com.imatalk.chatservice.dto.response.*;
import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.entity.Notification;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.event.Event;
import com.imatalk.chatservice.event.EventName;
import com.imatalk.chatservice.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.imatalk.chatservice.dto.response.ConversationChatHistoryDTO.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final ConversationService conversationService;
    private final MessageService messageService;

    @Value("${USER_TOPIC}")
    private String USER_TOPIC;

    public ResponseEntity<CommonResponse> createDirectConversation(User currentUser, String otherUserId) {
        User otherUser = userService.getUserById(otherUserId);

        // check if conversation already exists
        boolean conversationExists = conversationService.checkIfConversationExistsBetween2Users(currentUser, otherUser);
        if (conversationExists) {
            throw new ApplicationException("Conversation already exists");
        }

        // create conversation
        List<User> members = List.of(currentUser, otherUser);
        Conversation directConversation = conversationService.createAndSaveConversationBetween2Users(currentUser, otherUser);

        // add conversation to each member's conversation list
        for (User member : members) {
            member.joinConversation(directConversation);
        }
        userService.saveAll(members);

        return ResponseEntity.ok(CommonResponse.success("Conversation created"));


    }

    public ResponseEntity<CommonResponse> sendMessage(User currentUser, SendMessageRequest request) {

        System.out.println("User id: " + currentUser.getId());
        System.out.println("Conversation id: " + request.getConversationId());
        System.out.println("Message content: " + request.getContent());
        Conversation directConversation = conversationService.getConversationById(request.getConversationId());

        boolean userInConversation = checkUserInConversation(directConversation, currentUser);
        if (!userInConversation) {
            throw new ApplicationException("User is not in the conversation");
        }
        // save the message to the database
        SendMessageResponse response = addMessageToConversation(currentUser, request, directConversation);

        if (response.isSuccess()) {
            return ResponseEntity.ok(CommonResponse.success("Message sent", response));
        } else {
            return ResponseEntity.ok(CommonResponse.error("Message sent failed"));
        }
    }

    private SendMessageResponse addMessageToConversation(User currentUser, SendMessageRequest request, Conversation directConversation) {

        Message message = messageService.createAndSaveMessage(currentUser, request, directConversation);

        //TODO: create MessageSender class to send message to the client
        // send the message to the client using websocket after saving the message to the database
//        messagingTemplate.convertAndSend(USER_TOPIC + "/" + request.getConversationId(), message);

        //TODO: please create another service using Kafka to deal with notification
        // send notification to the other user in the conversation
        // extract new method for this

        // update the conversation
        conversationService.addMessageAndSaveConversation(directConversation, message);
        for (User member : directConversation.getMembers()) {
//            if (!member.getId().equals(currentUser.getId())) {
            Event event = new Event();
            event.setUserId(member.getId());
            event.setName(EventName.NEW_MESSAGE);
            event.setPayload(message);
            messagingTemplate.convertAndSend(USER_TOPIC + "/" + member.getId(), event);
        }
        // when user sends a message, set the current conversation to be the conversation that the user is sending message to
        currentUser.setCurrentConversationId(directConversation.getId());

        userService.save(currentUser);

        return new SendMessageResponse(true, message.getCreatedAt());
    }

    private boolean checkUserInConversation(Conversation directConversation, User currentUser) {
        return directConversation.getMembers().stream()
                .anyMatch(user -> user.getId().equals(currentUser.getId()));
    }

    public ResponseEntity<CommonResponse> getConversationChatHistory(User currentUser, String conversationId, long messageNo) {
        //TODO: update the seen message number for the user when get messages in the conversation
        Conversation directConversation = conversationService.getConversationById(conversationId);
        if (!checkUserInConversation(directConversation, currentUser)) {
            throw new ApplicationException("User is not in the conversation");
        }

        List<Message> messages = conversationService.get100Messages(directConversation, messageNo);
        ConversationChatHistoryDTO dto = convertToDirectConversationDetailDTO(directConversation, messages, currentUser);

        // set the conversation to be the current conversation of the user
        currentUser.setCurrentConversationId(conversationId);
        userService.save(currentUser);

        // when user gets messages, set the seen message number of the user to be the last message number of the conversation
        //TODO: use kafka to send notification to the other user when this seen the message
        directConversation.updateUserSeenLatestMessage(currentUser); // update the seen message number of the user
        conversationService.save(directConversation);
        return ResponseEntity.ok(CommonResponse.success("Messages retrieved", dto));

    }

    private ConversationChatHistoryDTO convertToDirectConversationDetailDTO(Conversation conversation, List<Message> messages, User currentUser) {

        return new ConversationChatHistoryDTO(conversation, currentUser, messages);
    }



    public ResponseEntity<CommonResponse> createGroupConversation(User currentUser, CreateGroupConversationRequest request) {

        // TODO: you need to check if group name is null or empty
        String groupName = request.getGroupName();
        List<String> memberIds = request.getMemberIds();
        // add current user to the list of members
        //TODO: you need to check if members are friends of the current user (user can only create group conversation with friends)
        if (!memberIds.contains(currentUser.getId())) {
            memberIds.add(currentUser.getId());
        }

        List<User> members = userService.findAllByIds(memberIds);

        Conversation conversation = conversationService.createAndSaveGroupConversation(groupName, members);

        // join users to the conversation
        for (User member : members) {
            member.joinConversation(conversation);
        }
        userService.saveAll(members);

        return ResponseEntity.ok(CommonResponse.success("Group conversation created"));
    }


    public ResponseEntity<CommonResponse> getConversationIdWithOtherUser(User currentUser, String otherUserId) {
        User otherUser = userService.getUserById(otherUserId);
        Conversation conversation = conversationService.getConversationBetween2Users(currentUser, otherUser);

        CommonResponse commonResponse = null;
        if (conversation == null) {
            commonResponse = CommonResponse.error("Conversation not found");
        } else {
            commonResponse = CommonResponse.success("Conversation found", new ConversationInfoDTO(conversation, currentUser));
        }

        return ResponseEntity.ok(commonResponse);
    }
}
