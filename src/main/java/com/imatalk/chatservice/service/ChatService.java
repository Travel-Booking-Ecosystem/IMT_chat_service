package com.imatalk.chatservice.service;

import com.imatalk.chatservice.dto.request.SendMessageRequest;
import com.imatalk.chatservice.dto.response.*;
import com.imatalk.chatservice.dto.response.DirectConversationDetailDTO.MemberDTO.LastSeen;
import com.imatalk.chatservice.entity.DirectConversation;
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
        DirectConversation directConversation = directConversationService.createAndSaveConversationBetween2Users(currentUser, otherUser);

        // add conversation to each member's conversation list
        for (User member : members) {
            member.joinConversation(directConversation);
        }
        userService.saveAll(members);

        return ResponseEntity.ok(CommonResponse.success("Conversation created"));


    }

    public ResponseEntity<CommonResponse> getProfile(User user) {
        // get some recent conversations
        List<DirectConversationInfoDTO> directConversationDTOs = getRecentDirectConversationInfo(user);

        // prepare the user profile
        UserProfile userProfile = new UserProfile(user);

        // add the recent conversations to the user profile
        userProfile.setDirectConversationList(directConversationDTOs);

        CommonResponse response = CommonResponse.success("Profile retrieved", userProfile);
        return ResponseEntity.ok(response);
    }

    private List<DirectConversationInfoDTO> getRecentDirectConversationInfo(User user) {
        // only get some recent conversations, if the user has too little conversations, get all of them
        int recentConversationNumber = Math.min(NUMBER_OF_CONVERSATION_PER_REQUEST, user.getDirectConversationInfoList().size());
        List<DirectConversation> directConversations = directConversationService.getConversationListOfUser(user, recentConversationNumber);

        List<DirectConversationInfoDTO> directConversationDTOs =
                convertToDirectConversationInfoDTOs(directConversations, user);

        return directConversationDTOs;
    }

    private List<DirectConversationInfoDTO> convertToDirectConversationInfoDTOs(List<DirectConversation> directConversations, User user) {
        // get all last sent message ids of those conversations
        List<String> lastSentMessageIds = directConversations.stream()
                .map(DirectConversation::getLastMessageId)
                .filter(Objects::nonNull)
                .toList();

        // get all last sent messages of those conversations from database
        // format to a map for faster lookup later
        Map<String, Message> lastSentMessages = messageService.findAllByIds(lastSentMessageIds)
                .stream()
                .collect(Collectors.toMap(Message::getId, Function.identity()));

        List<DirectConversationInfoDTO> result = new ArrayList<>();


        // convert each conversation to a DTO
        for (DirectConversation conversation : directConversations) {
            User otherUser = directConversationService.getTheOtherUserInConversation(user, conversation); // get the other user in the conversation to display
            Message lastMessage = lastSentMessages.get(conversation.getLastMessageId()); // get the last sent message of the conversation to display
            DirectConversationInfoDTO directConversationDTO = new DirectConversationInfoDTO(conversation, otherUser, lastMessage);
            // check if the current user has seen all messages in the conversation
            boolean currentUserSeenAllMessages = conversation.getLastSeenMessageNoOfMember(user.getId()) == conversation.getLastMessageNo();
            directConversationDTO.setUnread(!currentUserSeenAllMessages);
            result.add(directConversationDTO);
        }

        return result;
    }

    private static void moveConversationWitHIdToTopOfList(List<DirectConversationInfoDTO> list, String conversationId) {
        DirectConversationInfoDTO conversation = list.stream()
                .filter(dto -> dto.getId().equals(conversationId))
                .findFirst()
                .orElse(null);

        if (conversation != null) {
            list.removeIf(dto -> dto.getId().equals(conversationId));
            list.add(0, conversation);
        }
    }


    public ResponseEntity<CommonResponse> sendDirectMessage(User currentUser, SendMessageRequest request) {
        DirectConversation directConversation = directConversationService.getConversationById(request.getConversationId());

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


    private SendMessageResponse addMessageToConversation(User currentUser, SendMessageRequest request, DirectConversation directConversation) {

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


    private boolean checkUserInConversation(DirectConversation directConversation, User currentUser) {
        return directConversation.getMembers().stream()
                .anyMatch(user -> user.getId().equals(currentUser.getId()));
    }

    public ResponseEntity<CommonResponse> getDirectConversationDetail(User currentUser, String conversationId, long messageNo) {
        //TODO: update the seen message number for the user when get messages in the conversation
        DirectConversation directConversation = directConversationService.getConversationById(conversationId);
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

    private DirectConversationDetailDTO convertToDirectConversationDetailDTO(DirectConversation directConversation, List<Message> messages, User currentUser) {
        String conversationId = directConversation.getId();
        Map<String, MemberDTO> members = getMembersInConversation(directConversation);
        Map<String, DirectMessageDTO> messageDTOs = convertMessagesToDTO(messages, currentUser);

        DirectConversationDetailDTO dto = new DirectConversationDetailDTO();
        dto.setConversationId(conversationId);
        dto.setMembers(members);
        dto.setMessages(messageDTOs);

        // set name and avatar of the conversation by getting from the other user
        User otherUser = directConversationService.getTheOtherUserInConversation(currentUser, directConversation);
        dto.setConversationName(otherUser.getDisplayName());
        dto.setConversationAvatar(otherUser.getAvatar());

        return dto;


    }

    private Map<String, MemberDTO> getMembersInConversation(DirectConversation directConversation) {
        Map<String, MemberDTO> map = new HashMap<>();
        for (User member : directConversation.getMembers()) {
            MemberDTO otherUser = new MemberDTO(member);
            LastSeen lastSeen = new LastSeen(directConversation.getLastSeenMessageNoOfMember(member.getId()));
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
}
