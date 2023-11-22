package com.imatalk.chatservice.service;

import com.imatalk.chatservice.dto.request.CreateGroupConversationRequest;
import com.imatalk.chatservice.dto.request.ReactMessageRequest;
import com.imatalk.chatservice.dto.request.SendMessageRequest;
import com.imatalk.chatservice.dto.request.UpdateConversationSettingRequest;
import com.imatalk.chatservice.dto.response.*;
import com.imatalk.chatservice.dto.response.ConversationDetailsDTO.MessageDTO;
import com.imatalk.chatservice.entity.*;
import com.imatalk.chatservice.event.FriendRequestAcceptedEvent;
import com.imatalk.chatservice.event.NewRegisteredUserEvent;
import com.imatalk.chatservice.event.UserProfileUpdatedEvent;
import com.imatalk.chatservice.exception.ApplicationException;
import com.imatalk.chatservice.mongoRepository.ChatUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    // TODO: create Notification Service to send WS messages to the client
    @Value("${USER_TOPIC}")
    private String USER_TOPIC;

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final ChatUserRepository chatUserRepository;
    private final KafkaProducerService kafkaProducerService;


    public void createDirectConversation(FriendRequestAcceptedEvent event) {
        String receiverId = event.getReceiver().getId();
        String senderId = event.getSenderId();

        System.out.println("RECEIVER ID: " + receiverId);
        System.out.println("SENDER ID: " + senderId);


        ChatUser receiver = getChatUserById(receiverId);
        ChatUser sender = getChatUserById(senderId);

        System.out.println("RECEIVER DATA: ");
        System.out.println(receiver);
        System.out.println("SENDER DATA: ");
        System.out.println(sender);

        // check if conversation already exists
        boolean conversationExists = conversationService.checkIfConversationExistsBetween2Users(receiver, sender);
        if (conversationExists) {
            throw new ApplicationException("Conversation already exists");
        }

        // create conversation
        List<ChatUser> members = List.of(receiver, sender);


        Conversation directConversation = conversationService.createAndSaveConversationBetween2Users(List.of(receiver, sender));

        // produce NEW_CONVERSATION event for each user
        for (ChatUser member : members) {
            kafkaProducerService.sendNewConversationEvent(new ConversationInfoDTO(directConversation, member), member.getId());
        }

    }

    private ChatUser getChatUserById(String receiverId) {
        return chatUserRepository.findById(receiverId).orElseThrow(() -> new ApplicationException("User not found"));
    }

    public ResponseEntity<CommonResponse> sendMessage(String currentUserId, SendMessageRequest request) {
        ChatUser currentUser = getChatUserById(currentUserId);
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
            return ResponseEntity.ok(CommonResponse.success("Message sent successfully", response));
        } else {
            return ResponseEntity.ok(CommonResponse.error("Message sent failed"));
        }
    }

    private SendMessageResponse addMessageToConversation(ChatUser currentUser, SendMessageRequest request, Conversation conversation) {

        Message message = messageService.createAndSaveMessage(currentUser, request, conversation);
        // update the conversation
        conversationService.addMessageAndSaveConversation(conversation, message);

        // if message replies to another message, send notification to the user who sent the message that is replied
        if (message.getRepliedMessageId() != null && conversation.isGroupConversation()) {
            produceGroupMessageRepliedEvent(message, conversation,  currentUser);
        }

        kafkaProducerService.sendNewMessageEvent(new MessageDTO(message), conversation.getMemberIds());
        // when user sends a message, set the current conversation to be the conversation that the user is sending message to
        currentUser.setCurrentConversationId(conversation.getId());

        chatUserRepository.save(currentUser);

        return SendMessageResponse.builder()
                .success(true)
                .id(message.getId())
                .tempId(request.getTempId())
                .messageNo(message.getMessageNo())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private void produceGroupMessageRepliedEvent(Message message, Conversation conversation, ChatUser currentUser) {

        Message repliedMessage = messageService.findMessageById(message.getRepliedMessageId());
        kafkaProducerService.sendGroupMessageRepliedEvent(repliedMessage, conversation, currentUser);

    }

    private boolean checkUserInConversation(Conversation directConversation, ChatUser currentUser) {
        return directConversation.getMembers().stream()
                .anyMatch(user -> user.getId().equals(currentUser.getId()));
    }

    public ResponseEntity<CommonResponse> getConversationDetails(String currentUserId, String conversationId, long messageNo) {
        ChatUser currentUser = getChatUserById(currentUserId);
        //TODO: update the seen message number for the user when get messages in the conversation
        Conversation directConversation = conversationService.getConversationById(conversationId);

        if (!checkUserInConversation(directConversation, currentUser)) {
            throw new ApplicationException("User is not in the conversation");
        }

        List<Message> messages = conversationService.get100Messages(directConversation, messageNo);
        ConversationDetailsDTO dto = convertToDirectConversationDetailDTO(directConversation, messages, currentUser);

        // set the conversation to be the current conversation of the user
        currentUser.setCurrentConversationId(conversationId);
        chatUserRepository.save(currentUser);

        // when user gets messages, set the seen message number of the user to be the last message number of the conversation
        //TODO: use kafka to send notification to the other user when this seen the message
        conversationService.updateUserSenLatestMessage(directConversation, currentUser);
        conversationService.save(directConversation);
        return ResponseEntity.ok(CommonResponse.success("Messages retrieved", dto));

    }

    private ConversationDetailsDTO convertToDirectConversationDetailDTO(Conversation conversation, List<Message> messages, ChatUser currentUser) {

        return new ConversationDetailsDTO(conversation, currentUser, messages);
    }



    public ResponseEntity<CommonResponse> createGroupConversation(String currentUserId, CreateGroupConversationRequest request) {

//        User currentUser = userService.getUserById(currentUserId);
        ChatUser currentUser = getChatUserById(currentUserId);

        String groupName = request.getGroupName();
        List<String> memberIds = request.getMemberIds();
        // add current user to the list of members
        //TODO: you need to check if members are friends of the current user (user can only create group conversation with friends)
        if (!memberIds.contains(currentUser.getId())) {
            memberIds.add(currentUser.getId());
        }

        List<ChatUser> members = chatUserRepository.findAllById(memberIds);

        Conversation conversation = conversationService.createAndSaveGroupConversation(groupName, members);

        for (ChatUser member : conversation.getMembers()) {
            // send notification to each member
            kafkaProducerService.sendNewConversationEvent(new ConversationInfoDTO(conversation, member), member.getId());

        }
        return ResponseEntity.ok(CommonResponse.success("Group conversation created"));
    }


    public ResponseEntity<CommonResponse> getConversationIdWithOtherUser(String currentUserId, String otherUserId) {

        ChatUser currentUser = getChatUserById(currentUserId);
        ChatUser otherUser = getChatUserById(otherUserId);


        Conversation conversation = conversationService.getConversationBetween2Users(currentUser, otherUser);

        CommonResponse commonResponse = null;
        if (conversation == null) {
            commonResponse = CommonResponse.error("Conversation not found");
        } else {
            commonResponse = CommonResponse.success("Conversation found", new ConversationInfoDTO(conversation, currentUser));
        }

        return ResponseEntity.ok(commonResponse);
    }

    public ResponseEntity<CommonResponse> getConversationList(String currentUserId) {

        ChatUser currentUser = getChatUserById(currentUserId);
        log.info("Current user: " + currentUser);
            List<Conversation> conversations = conversationService.getConversationListOrderByLastUpdateAtDesc(currentUser);
        List<ConversationInfoDTO> conversationInfoDTOList = new ArrayList<>();

        for (Conversation conversation : conversations) {
            ConversationInfoDTO conversationInfoDTO = new ConversationInfoDTO(conversation, currentUser);
            conversationInfoDTOList.add(conversationInfoDTO);
        }

        LeftSidebarDTO dto = new LeftSidebarDTO();
        dto.setConversations(conversationInfoDTOList);
        dto.setCurrentConversationId(currentUser.getCurrentConversationId());
        return ResponseEntity.ok(CommonResponse.success("Conversation list retrieved", dto));
    }

    public void createChatUser(NewRegisteredUserEvent event) {

        ChatUser chatUser = ChatUser.builder()
                .currentConversationId(null)
                .displayName(event.getDisplayName())
                .username(event.getUsername())
                .avatar(event.getAvatar())
                .id(event.getUserId())
                .build();

        chatUserRepository.save(chatUser);
    }

    public ResponseEntity<CommonResponse> test(String id) {
        ChatUser chatUser = chatUserRepository.findById(id).orElse(null);
        return ResponseEntity.ok(CommonResponse.success("User found", chatUser));

    }

    public ResponseEntity<CommonResponse> updateConversationSetting(String currentUserId, UpdateConversationSettingRequest request) {

        ChatUser currentUser = getChatUserById(currentUserId);
        String conversationId = request.getConversationId();
        Conversation conversation = conversationService.getConversationById(conversationId);

        if (!checkUserInConversation(conversation, currentUser)) {
            throw new ApplicationException("User is not in the conversation");
        }




        return conversationService.updateConversationSetting(conversation, request);
    }

    public ResponseEntity<CommonResponse> reactMessage(String currentUserId, ReactMessageRequest request) {


        ChatUser currentUser = getChatUserById(currentUserId);
        String conversationId = request.getConversationId();
        Conversation conversation = conversationService.getConversationById(conversationId);

        if (!checkUserInConversation(conversation, currentUser)) {
            throw new ApplicationException("User is not in the conversation");
        }

        Message message = messageService.findMessageById(request.getMessageId());
        if (message == null) {
            throw new ApplicationException("Message not found");
        }

//        if (message.getSenderId().equals(currentUserId)) {
//            throw new ApplicationException("User cannot react to his/her own message");
//        }

        Map<String, Object> response = messageService.reactMessage(message, request, currentUser);
        boolean isUnreact = (boolean) response.get("isUnreact");
        Message reactMessage = (Message) response.get("message");

        if (reactMessage == null) {
            throw new ApplicationException("Message reaction failed");
        }

        ConversationInfoDTO conversationInfoDTO = new ConversationInfoDTO(conversation, currentUser);

        // send react event to the other members in the conversation
        kafkaProducerService.sendReactionMessageEvent(reactMessage, conversationInfoDTO, currentUser, conversation.getMemberIds(), isUnreact);
        if (isUnreact) {
            return ResponseEntity.ok(CommonResponse.success("Message unreacted"));
        } else {
            return ResponseEntity.ok(CommonResponse.success("Message reacted"));
        }
    }

    public void updateChatUser(UserProfileUpdatedEvent event) {
        String userId = event.getUserId();


        ChatUser chatUser = getChatUserById(userId);

        chatUser.setAvatar(event.getAvatar());
        chatUser.setDisplayName(event.getDisplayName());

        chatUserRepository.save(chatUser);
    }
}
