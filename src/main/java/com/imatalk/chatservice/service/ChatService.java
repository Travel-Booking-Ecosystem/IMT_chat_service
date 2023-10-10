package com.imatalk.chatservice.service;

import com.imatalk.chatservice.dto.request.CreateGroupConversationRequest;
import com.imatalk.chatservice.dto.request.SendMessageRequest;
import com.imatalk.chatservice.dto.response.*;
import com.imatalk.chatservice.dto.response.DirectConversationDetailDTO.MemberDTO.LastSeen;
import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.entity.Notification;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.imatalk.chatservice.dto.response.DirectConversationDetailDTO.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final DirectConversationService directConversationService;
    private final MessageService messageService;
    private final String DIRECT_CONVERSATION_TOPIC = "/topic/chat";
    private final String NOTIFICATION_TOPIC = "/topic/notification";
    private final int NUMBER_OF_CONVERSATION_PER_REQUEST = 10; //TODO: move this to application.properties

    // TODO: move all the logic of direct conversation to DirectConversationService
    public ResponseEntity<CommonResponse> createDirectConversation(User currentUser, String otherUserId) {
        User otherUser = userService.getUserById(otherUserId);

        // check if conversation already exists
        boolean conversationExists = directConversationService.checkIfConversationExistsBetween2Users(currentUser, otherUser);
        if (conversationExists) {
            throw new ApplicationException("Conversation already exists");
        }

        // create conversation
        List<User> members = List.of(currentUser, otherUser);
        Conversation directConversation = directConversationService.createAndSaveConversationBetween2Users(currentUser, otherUser);

        // add conversation to each member's conversation list
        for (User member : members) {
            member.joinConversation(directConversation);
        }
        userService.saveAll(members);

        return ResponseEntity.ok(CommonResponse.success("Conversation created"));


    }

    public ResponseEntity<CommonResponse> getProfile(User user) {
        // get some recent conversations
        List<ConversationInfoDTO> directConversationDTOs = getRecentDirectConversationInfo(user);

        // prepare the user profile
        UserProfile userProfile = new UserProfile(user);

        // add the recent conversations to the user profile
        userProfile.setDirectConversationList(directConversationDTOs);

        CommonResponse response = CommonResponse.success("Profile retrieved", userProfile);
        return ResponseEntity.ok(response);
    }

    private List<ConversationInfoDTO> getRecentDirectConversationInfo(User user) {
        // only get some recent conversations, if the user has too little conversations, get all of them
        int recentConversationNumber = Math.min(NUMBER_OF_CONVERSATION_PER_REQUEST, user.getDirectConversationInfoList().size());
        List<Conversation> directConversations = directConversationService.getConversationListOfUser(user, recentConversationNumber);

        List<ConversationInfoDTO> directConversationDTOs = directConversations.stream()
                .map(conversation -> new ConversationInfoDTO(conversation, user))
                .collect(Collectors.toList());

        return directConversationDTOs;
    }



    public ResponseEntity<CommonResponse> sendDirectMessage(User currentUser, SendMessageRequest request) {
        Conversation directConversation = directConversationService.getConversationById(request.getConversationId());

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
        messagingTemplate.convertAndSend(DIRECT_CONVERSATION_TOPIC + "/" + request.getConversationId(), message);

        //TODO: please create another service using Kafka to deal with notification
        // send notification to the other user in the conversation
        // extract new method for this
        for (User member : directConversation.getMembers()) {
//            if (!member.getId().equals(currentUser.getId())) {
            Notification notification = new Notification();
            notification.setUserId(member.getId());
            notification.setType(Notification.NotificationType.D_M);
            notification.setContent(message.getContent());
            notification.setCreatedAt(message.getCreatedAt());
            notification.setConversationId(directConversation.getId());
            notification.setSenderId(currentUser.getId());

            messagingTemplate.convertAndSend(NOTIFICATION_TOPIC + "/" + member.getId(), notification);
//            }
        }

        // update the conversation
        directConversationService.addMessageAndSaveConversation(directConversation, message);

        // when user sends a message, set the current conversation to be the conversation that the user is sending message to
        currentUser.setCurrentConversationId(directConversation.getId());

        userService.save(currentUser);

        return new SendMessageResponse(true, message.getCreatedAt());
    }


    private boolean checkUserInConversation(Conversation directConversation, User currentUser) {
        return directConversation.getMembers().stream()
                .anyMatch(user -> user.getId().equals(currentUser.getId()));
    }

    public ResponseEntity<CommonResponse> getDirectConversationDetail(User currentUser, String conversationId, long messageNo) {
        //TODO: update the seen message number for the user when get messages in the conversation
        Conversation directConversation = directConversationService.getConversationById(conversationId);
        if (!checkUserInConversation(directConversation, currentUser)) {
            throw new ApplicationException("User is not in the conversation");
        }

        List<Message> messages = directConversationService.get100Messages(directConversation, messageNo);
        DirectConversationDetailDTO dto = convertToDirectConversationDetailDTO(directConversation, messages, currentUser);

        // set the conversation to be the current conversation of the user
        currentUser.setCurrentConversationId(conversationId);
        userService.save(currentUser);

        // when user gets messages, set the seen message number of the user to be the last message number of the conversation
        //TODO: use kafka to send notification to the other user when this seen the message
        directConversation.updateUserSeenLatestMessage(currentUser); // update the seen message number of the user
        directConversationService.save(directConversation);
        return ResponseEntity.ok(CommonResponse.success("Messages retrieved", dto));

    }

    private DirectConversationDetailDTO convertToDirectConversationDetailDTO(Conversation conversation, List<Message> messages, User currentUser) {


        Map<String, MemberDTO> members = getMembersInConversation(conversation);
        Map<String, DirectMessageDTO> messageDTOs = convertMessagesToDTO(messages, currentUser);

        DirectConversationDetailDTO dto = new DirectConversationDetailDTO(conversation, currentUser);
        dto.setMembers(members);
        dto.setMessages(messageDTOs);

        return dto;


    }

    private static Map<String, MemberDTO> getMembersInConversation(Conversation conversation) {
        Map<String, MemberDTO> map = new HashMap<>();
        for (User member : conversation.getMembers()) {
            MemberDTO otherUser = new MemberDTO(member);
            MemberDTO.LastSeen lastSeen = new MemberDTO.LastSeen(conversation.getLastSeenMessageNoOfMember(member.getId()));
            otherUser.setLastSeen(lastSeen);
            map.put(member.getId(), otherUser);
        }


        return map;
    }




    private Map<String, DirectMessageDTO> convertMessagesToDTO(List<Message> messages, User currentUser) {
        Map<String, DirectMessageDTO> map = new LinkedHashMap<>(); // using LinkedHashMap to preserve the order of insertion (the order of messages)
        for (Message message : messages) {
            DirectMessageDTO dto = new DirectMessageDTO(message, currentUser);
            map.put(dto.getId(), dto);
        }
        return map;
    }


    public ResponseEntity<CommonResponse> test(User currentUser, SendMessageRequest request) {
        return sendDirectMessage(currentUser, request);
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

        Conversation conversation = directConversationService.createAndSaveGroupConversation(groupName, members);

        // join users to the conversation
        for (User member : members) {
            member.joinConversation(conversation);
        }
        userService.saveAll(members);

        return ResponseEntity.ok(CommonResponse.success("Group conversation created"));
    }
}
