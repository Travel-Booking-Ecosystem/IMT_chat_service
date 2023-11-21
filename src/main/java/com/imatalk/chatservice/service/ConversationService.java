package com.imatalk.chatservice.service;


import com.imatalk.chatservice.dto.request.UpdateConversationSettingRequest;
import com.imatalk.chatservice.dto.response.CommonResponse;
import com.imatalk.chatservice.entity.ChatUser;
import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.enums.ConversationStatus;
import com.imatalk.chatservice.enums.DefaultReaction;
import com.imatalk.chatservice.exception.ApplicationException;
import com.imatalk.chatservice.mongoRepository.ChatUserRepository;
import com.imatalk.chatservice.mongoRepository.ConversationRepository;
import com.imatalk.chatservice.mongoRepository.MessageRepo;
import com.imatalk.chatservice.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static com.imatalk.chatservice.enums.ConversationStatus.NEW;

@Service
@RequiredArgsConstructor
//TODO: you will need to rename this class to ConversationService, since there is only one service for both direct and group conversation
//TODO: change the repositiory as well
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepo messageRepo;
    private final ChatUserRepository chatUserRepository;

    public boolean checkIfConversationExistsBetween2Users(ChatUser user1, ChatUser user2) {
        Conversation conversationBetween2Users = getConversationBetween2Users(user1, user2);
        return conversationBetween2Users != null;
    }

    public Conversation createAndSaveConversationBetween2Users(List<ChatUser> users) {

        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID().toString())
                .members(users)
                .status(NEW)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        return conversationRepository.save(conversation);
    }

    public Conversation getConversationById(String id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Conversation not found for id " + id));
    }

    public void addMessageAndSaveConversation(Conversation directConversation, Message message) {
        addMessageToConversation(directConversation, message);
        directConversation.setStatus(ConversationStatus.ACTIVE);
        conversationRepository.save(directConversation);

    }

    public void addMessageToConversation(Conversation conversation, Message message) {
        // set messageNo to be the next number in the sequence
        long lastMessageNo = conversation.getLastMessageNo();
        long newMessageNo = lastMessageNo + 1;

        // update the seen message number for the sender of the message
        Map<String, Long> seenMessageTracker = conversation.getSeenMessageTracker();

        for (ChatUser member : conversation.getMembers()) {
            // if the user is the sender of the message, set the seen message number to be the message number of the message
            // otherwise, set the seen message number to be the last seen message number of the user
            long seenMessageNo = member.getId().equals(message.getSenderId()) ? newMessageNo : seenMessageTracker.getOrDefault(member.getId(), 0L);
            seenMessageTracker.put(member.getId(), seenMessageNo);

        }

        conversation.setLastMessage(new Conversation.LastMessage(message, newMessageNo));
        conversation.setLastUpdatedAt(message.getCreatedAt());

    }


    public List<Message> get100Messages(Conversation directConversation, long messageNo) {
        List<Message> messages = new ArrayList<Message>();
        if (messageNo == -1) {
            messages = getLast100MessagesOfAConversation(directConversation);
        } else {
            messages = getPrevious100MessagesByMessageNo(directConversation, messageNo);
        }
        return messages;
    }

    private List<Message> getPrevious100MessagesByMessageNo(Conversation directConversation, long messageNo) {
        // get the last 100 messages from the messageNo specified in the conversation
        return messageRepo.findAllByConversationIdAndMessageNoGreaterThanEqualOrderByCreatedAt(directConversation.getId(), messageNo - 100);
    }

    private List<Message> getLast100MessagesOfAConversation(Conversation directConversation) {
        // get the last 100 messages of the conversation
        long lastMessageNo = directConversation.getLastMessageNo();
        return messageRepo.findAllByConversationIdAndMessageNoGreaterThanEqualOrderByCreatedAt(directConversation.getId(), lastMessageNo - 100);
    }

    public void save(Conversation directConversation) {
        conversationRepository.save(directConversation);
    }

    public Conversation createAndSaveGroupConversation(String groupName, List<ChatUser> members) {
        Conversation conversation = createEmptyConversation();

        List<String> ids = members.stream().map(ChatUser::getId).toList();
        List<ChatUser> conversationMembers = chatUserRepository.findAllById(ids);
        // set users in the conversation
        conversation.setMembers(conversationMembers);
        conversation.setGroupName(groupName);

        conversation.setGroupAvatar(Utils.generateAvatarUrl(groupName));
        conversation.setGroupConversation(true);
        conversation.setStatus(NEW);

        return conversationRepository.save(conversation);

    }


    private Conversation createEmptyConversation() {
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID().toString())
                .members(List.of())
                .createdAt(LocalDateTime.now())
                .lastUpdatedAt(LocalDateTime.now())
                .status(NEW)
                .seenMessageTracker(new HashMap<>())
                .build();
        return conversation;
    }

    public Conversation getConversationBetween2Users(ChatUser user1, ChatUser user2) {
        // get the conversation between 2 users
        List<Conversation> conversations = conversationRepository.findAllByMembersInAndIsGroupConversation(List.of(user1, user2), false);

        // find the conversation that has only 2 members and their ids are the ids of the 2 users
        return conversations.stream().filter(c -> {
            List<ChatUser> members = c.getMembers();
            return members.size() == 2 && members.stream().allMatch(m -> m.getId().equals(user1.getId()) || m.getId().equals(user2.getId()));
        }).findFirst().orElse(null);

    }

    public void updateUserSenLatestMessage(Conversation conversation, ChatUser currentUser) {
        conversation.updateUserSeenLatestMessage(currentUser.getId()); // update the seen message number of the user
        long lastMessageNo = conversation.getLastMessageNo();
        // this line update for the data in-memory of the app
        conversation.getSeenMessageTracker().put(currentUser.getId(), lastMessageNo);


        conversationRepository.save(conversation);

    }

    public List<Conversation> getConversationListOrderByLastUpdateAtDesc(ChatUser currentUser) {
        List<ChatUser> conversationMembers = new ArrayList<>();
        conversationMembers.add(currentUser);
        return conversationRepository.findAllByMembersInOrderByLastUpdatedAtDesc(conversationMembers);
    }

    public ResponseEntity<CommonResponse> updateConversationSetting(Conversation conversation, UpdateConversationSettingRequest request) {
        Conversation.ConversationSetting setting = conversation.getConversationSetting();

        if (setting == null) {
            setting = new Conversation.ConversationSetting();
        }

        if (request.getWallpaper() != null)   setting.setWallpaper(request.getWallpaper());
        if (request.getThemeColor() != null) setting.setThemeColor(request.getThemeColor());
        if (request.getDefaultReaction() != null) {
            DefaultReaction defaultReaction = DefaultReaction.valueOf(request.getDefaultReaction());
            setting.setDefaultReaction(defaultReaction);
        }

        conversation.setConversationSetting(setting);
        conversationRepository.save(conversation);


        return ResponseEntity.ok(CommonResponse.success("Conversation setting updated", conversation));

    }
}
