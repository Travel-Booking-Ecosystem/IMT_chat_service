package com.imatalk.chatservice.service;

import com.imatalk.chatservice.dto.request.SendMessageRequest;
import com.imatalk.chatservice.dto.response.*;
import com.imatalk.chatservice.dto.response.DirectConversationDetailDTO.MemberDTO.LastSeen;
import com.imatalk.chatservice.entity.DirectConversation;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.exception.ApplicationException;
import com.imatalk.chatservice.repository.MessageRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final int NUMBER_OF_CONVERSATION_PER_REQUEST = 10; //TODO: move this to application.properties

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
            result.add(directConversationDTO);
        }

        return result;
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

        // update the conversation
        directConversationService.addMessageAndSaveConversation(directConversation, message);

        // move the conversation to the top of the list
        List<User> members = directConversation.getMembers();
        for (User member : members) {
            member.moveConversationToTop(directConversation);
        }
        userService.saveAll(members);

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
        Map<String, MemberDTO> map  = new HashMap<>();
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
