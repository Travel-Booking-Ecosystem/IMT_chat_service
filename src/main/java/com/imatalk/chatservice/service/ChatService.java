package com.imatalk.chatservice.service;

import com.imatalk.chatservice.dto.request.SendMessageRequest;
import com.imatalk.chatservice.dto.response.*;
import com.imatalk.chatservice.dto.response.DirectConversationMessagesDTO.MemberDTO;
import com.imatalk.chatservice.dto.response.DirectConversationMessagesDTO.MemberDTO.LastSeen;
import com.imatalk.chatservice.entity.DirectConversation;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.exception.ApplicationException;
import com.imatalk.chatservice.repository.DirectConversationRepo;
import com.imatalk.chatservice.repository.MessageRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.imatalk.chatservice.dto.response.DirectConversationMessagesDTO.*;
import static com.imatalk.chatservice.entity.DirectConversation.createDefaultSeenMessageTracker;

@Service
@RequiredArgsConstructor
public class ChatService {

    // TODO: should it split the logic of direct conversation and group conversation into 2 services?
    private final UserService userService;
    // TODO: directConversationService
    private final DirectConversationRepo directConversationRepo;
    // TODO: directMessageService
    private final MessageRepo messageRepo;

    private final int NUMBER_OF_CONVERSATION_PER_REQUEST = 10;

    public ResponseEntity<CommonResponse> createDirectConversation(User currentUser, String otherUserId) {
        User otherUser = userService.getUserById(otherUserId);

        // check if conversation already exists
        boolean conversationExists = directConversationRepo.findByMembersIn(List.of(currentUser, otherUser)).isPresent();
        if (conversationExists) {
            throw new RuntimeException("Conversation already exists between 2 users");
        }


        // create conversation
        List<User> members = List.of(currentUser, otherUser);
        DirectConversation directConversation = DirectConversation.builder()
                .createdAt(LocalDateTime.now())
                .members(members)
                .seenMessageTracker(createDefaultSeenMessageTracker(members))
                .messages(new ArrayList<>())
                .build();

        directConversation = directConversationRepo.save(directConversation);

        // add conversation to each member's conversation list
        for (User member : members) {
            member.joinConversation(directConversation);
        }
        userService.saveAll(members);

        return ResponseEntity.ok(CommonResponse.success("Conversation created"));
    }

    public ResponseEntity<CommonResponse> getProfile(User user) {
        // get some recent conversations
        List<DirectConversationInfoDTO> directConversationDTOs = getRecentDirectConversations(user);

        // prepare the user profile
        UserProfile userProfile = new UserProfile(user);

        // add the recent conversations to the user profile
        userProfile.setDirectConversationList(directConversationDTOs);

        CommonResponse response = CommonResponse.success("Profile retrieved", userProfile);
        return ResponseEntity.ok(response);
    }

    private List<DirectConversationInfoDTO> getRecentDirectConversations(User user) {
        // only get some recent conversations, if the user has too little conversations, get all of them
        int recentConversationNumber = Math.min(NUMBER_OF_CONVERSATION_PER_REQUEST, user.getDirectConversationInfoList().size());
        List<String> conversationIds = user.getDirectConversationInfoList().subList(0, recentConversationNumber);

        // get all recent conversations from database
        List<DirectConversation> directConversations =
                directConversationRepo.findAllById(conversationIds);

        List<DirectConversationInfoDTO> directConversationDTOs =
                convertToDirectConversationInfoTOs(directConversations, user);

        return directConversationDTOs;
    }

    private List<DirectConversationInfoDTO> convertToDirectConversationInfoTOs(List<DirectConversation> directConversations, User user) {
        // get all last sent message ids of those conversations
        List<String> lastSentMessageIds = directConversations.stream()
                .map(DirectConversation::getLastMessageId)
                .filter(Objects::nonNull)
                .toList();

        // get all last sent messages of those conversations from database
        // format to a map for faster lookup later
        Map<String, Message> lastSentMessages = messageRepo
                .findAllById(lastSentMessageIds)
                .stream()
                .collect(Collectors.toMap(Message::getId, Function.identity()));

        List<DirectConversationInfoDTO> result = new ArrayList<>();


        // convert each conversation to a DTO
        for (DirectConversation conversation : directConversations) {

            User otherUser = getTheOtherUserInConversation(user, conversation);
            Message lastMessage = lastSentMessages.get(conversation.getLastMessageId());
            DirectConversationInfoDTO directConversationDTO = new DirectConversationInfoDTO(conversation, otherUser, lastMessage);

            result.add(directConversationDTO);
        }

        return result;
    }

    private static User getTheOtherUserInConversation(User user, DirectConversation conversation) {

        // there can be only 2 members in a direct conversation
        // find the other user in the conversation
        User otherUser = conversation.getMembers().get(0);
        // if the first member is the current user, then the other user is the second member
        if (otherUser.getId().equals(user.getId())) {
            otherUser = conversation.getMembers().get(1);
        }
        return otherUser;
    }


    public ResponseEntity<CommonResponse> sendDirectMessage(User currentUser, SendMessageRequest request) {
        DirectConversation directConversation = directConversationRepo.findById(request.getConversationId())
                .orElseThrow(() -> new ApplicationException("Conversation not found for id " + request.getConversationId()));

        boolean userInConversation = checkUserInConversation(directConversation, currentUser);
        if (!userInConversation) {
            throw new ApplicationException("User is not in the conversation");
        }

        SendMessageResponse response = addMessageToConversation(currentUser, request, directConversation);
        if (response.isSuccess()) {
            return ResponseEntity.ok(CommonResponse.success("Message sent", response));
        } else {
            return ResponseEntity.ok(CommonResponse.error("Message sent failed"));
        }
    }


    private SendMessageResponse addMessageToConversation(User currentUser, SendMessageRequest request, DirectConversation directConversation) {
        LocalDateTime now = LocalDateTime.now();
        //TODO: clean this code


        long lastMessageNoInConversation = directConversation.getLastMessageNo();
        Message message = createMessage(currentUser, request, now);
        message.setConversationId(directConversation.getId());
        message.setMessageNo(lastMessageNoInConversation + 1);
        message = messageRepo.save(message);

        directConversation.addMessage(message);
        directConversationRepo.save(directConversation);

        // move the conversation to the top of the list
        List<User> members = directConversation.getMembers();
        for (User member : members) {
            member.moveConversationToTop(directConversation);
        }
        userService.saveAll(members);
        return new SendMessageResponse(true, now);
    }

    private Message createMessage(User currentUser, SendMessageRequest request, LocalDateTime now) {
        return Message.builder()
                .senderId(currentUser.getId())
                .content(request.getContent())
                .repliedMessageId(request.getRepliedMessageId())
                .createdAt(now)
                .build();
    }

    private boolean checkUserInConversation(DirectConversation directConversation, User currentUser) {
        return directConversation.getMembers().stream()
                .anyMatch(user -> user.getId().equals(currentUser.getId()));
    }

    public ResponseEntity<CommonResponse> getDirectConversationMessages(User currentUser, String conversationId, long messageNo) {
        //TODO: update the seen message number for the user when get messages in the conversation
        DirectConversation directConversation = directConversationRepo.findById(conversationId)
                .orElseThrow(() -> new ApplicationException("Conversation not found for id " + conversationId));

        if (!checkUserInConversation(directConversation, currentUser)) {
            throw new ApplicationException("User is not in the conversation");
        }

        //TODO: this logic should be done by the direct conversation service
        List<Message> messages = get100Messages(directConversation, messageNo);
        Map<String, Long> seenMessageTracker = directConversation.getSeenMessageTracker();
        DirectConversationMessagesDTO dto = convertToDirectConversationMessagesDTO(directConversation, messages, seenMessageTracker, currentUser);

        return ResponseEntity.ok(CommonResponse.success("Messages retrieved", dto));

    }

    private DirectConversationMessagesDTO convertToDirectConversationMessagesDTO(DirectConversation directConversation, List<Message> messages, Map<String, Long> seenMessageTracker, User currentUser) {
        String conversationId = directConversation.getId();
        Map<String, MemberDTO> memberDTOs = convertMembersToDTO(directConversation, seenMessageTracker);
        Map<String, DirectMessageDTO> messageDTOs = convertMessagesToDTO(messages, currentUser);

        DirectConversationMessagesDTO dto = new DirectConversationMessagesDTO();
        dto.setConversationId(conversationId);
        dto.setMembers(memberDTOs);
        dto.setMessages(messageDTOs);


        return dto;


    }

    private Map<String, MemberDTO> convertMembersToDTO(DirectConversation directConversation, Map<String, Long> seenMessageTracker) {
        Map<String, MemberDTO> map = new HashMap<>();


        for (User member : directConversation.getMembers()) {
            MemberDTO memberDTO = new MemberDTO(member);

            String memberId = member.getId();
            // get the last message number that the user has seen in this conversation
            long lastMessageNoSeenByUser = seenMessageTracker.getOrDefault(memberId, 0L);
            memberDTO.setLastSeen(new LastSeen(lastMessageNoSeenByUser));

            map.put(memberId, memberDTO);
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

    private List<Message> get100Messages(DirectConversation directConversation, long messageNo) {
        List<Message> messages = new ArrayList<Message>();
        if (messageNo == -1) {
            messages = getLast100MessagesOfAConversation(directConversation);
        } else {
            messages = getPrevious100MessagesByMessageNo(directConversation, messageNo);
        }
        return messages;
    }

    private List<Message> getPrevious100MessagesByMessageNo(DirectConversation directConversation, long messageNo) {
        // get the last 100 messages from the messageNo specified in the conversation
        return messageRepo.findAllByConversationIdAndMessageNoGreaterThanEqualOrderByCreatedAt(directConversation.getId(), messageNo - 100);
    }

    private List<Message> getLast100MessagesOfAConversation(DirectConversation directConversation) {
        // get the last 100 messages of the conversation
        long lastMessageNo = directConversation.getLastMessageNo();
        return messageRepo.findAllByConversationIdAndMessageNoGreaterThanEqualOrderByCreatedAt(directConversation.getId(), lastMessageNo - 100);
    }
}
