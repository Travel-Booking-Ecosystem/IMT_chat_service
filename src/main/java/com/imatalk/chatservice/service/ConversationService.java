package com.imatalk.chatservice.service;


import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.entity.MessageSeen;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.enums.ConversationStatus;
import com.imatalk.chatservice.exception.ApplicationException;
import com.imatalk.chatservice.relationRepository.ConversationRepo;
import com.imatalk.chatservice.mongoRepository.MessageRepo;
import com.imatalk.chatservice.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.imatalk.chatservice.enums.ConversationStatus.NEW;

@Service
@RequiredArgsConstructor
//TODO: you will need to rename this class to ConversationService, since there is only one service for both direct and group conversation
//TODO: change the repositiory as well
@Slf4j
public class ConversationService {

    private final ConversationRepo conversationRepo;
    private final MessageRepo messageRepo;


    public boolean checkIfConversationExistsBetween2Users(User user1, User user2) {
        Conversation conversationBetween2Users = getConversationBetween2Users(user1, user2);
        return conversationBetween2Users != null;
    }

    public Conversation createAndSaveConversationBetween2Users(User user1, User user2) {
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID().toString())
                .members(List.of(user1, user2))
                .lastUpdatedAt(LocalDateTime.now())
                .messageSeenRecord(new ArrayList<>())
                .build();

        return conversationRepo.save(conversation);
    }

    public List<Conversation> getConversationListOfUser(User user, int numberOfConversations) {

        // get the first n conversation ids from the user's conversation list
        List<String> conversationIds = user.getConversations()
                .subList(0, numberOfConversations).stream()
                .map(Conversation::getId).toList();

        // get a number of conversations from database
        log.info("Get {} conversations from database", numberOfConversations);
        List<Conversation> directConversations = user.getConversations();
        // sort by last updated at
        directConversations.sort((c1, c2) -> c2.getLastUpdatedAt().compareTo(c1.getLastUpdatedAt()));

//        List<Conversation> directConversations = conversationRepo.findAllByIdInOrderByLastUpdatedAtDesc(conversationIds);
        log.info("Get {} conversations from database done", numberOfConversations);
        return directConversations;
    }


    public Conversation getConversationById(String id) {
        return conversationRepo.findById(id)
                .orElseThrow(() -> new ApplicationException("Conversation not found for id " + id));
    }

    public void addMessageAndSaveConversation(Conversation directConversation, Message message) {
        addMessageToConversation(directConversation, message);
        directConversation.setStatus(ConversationStatus.ACTIVE);
        conversationRepo.save(directConversation);

    }

    public void addMessageToConversation(Conversation conversation, Message message) {
        // set messageNo to be the next number in the sequence
        long lastMessageNo = conversation.getLastMessageNo();
        long newMessageNo = lastMessageNo + 1;

        // update the seen message number for the sender of the message
        Map<String, Long> seenMessageTracker = conversation.getSeenMessageTracker();

        for (User member : conversation.getMembers()) {
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
        conversationRepo.save(directConversation);
    }

    public Conversation createAndSaveGroupConversation(String groupName, List<User> members) {
        Conversation conversation = createEmptyConversation();

        // set users in the conversation
        conversation.setMembers(members);
        conversation.setGroupName(groupName);

        conversation.setGroupAvatar(Utils.generateAvatarUrl(groupName));
        conversation.setGroupConversation(true);

        return conversationRepo.save(conversation);

    }


    private Conversation createEmptyConversation() {
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID().toString())
                .members(List.of())
                .createdAt(LocalDateTime.now())
                .lastUpdatedAt(LocalDateTime.now())
                .status(NEW)
                .messageSeenRecord(new ArrayList<>())
                .build();
        return conversation;
    }

    public Conversation getConversationBetween2Users(User user1, User user2) {
        Conversation conversationBetween2Users = user1.getConversations()
                .stream().filter(c -> !c.isGroupConversation()) // only get direct conversations
                .filter(c -> c.getMembers()
                        // find the conversation that has user2 as a member
                        .stream()
                        .anyMatch(member -> member.getId().equals(user2.getId()))
                )
                .findFirst().orElse(null);

        return conversationBetween2Users;
    }

    public void updateUserSenLatestMessage(Conversation directConversation, User currentUser) {
        directConversation.updateUserSeenLatestMessage(currentUser.getId()); // update the seen message number of the user
        long lastMessageNo = directConversation.getLastMessageNo();
        // this line update for the data in-memory of the app
        directConversation.getSeenMessageTracker().put(currentUser.getId(), lastMessageNo);

        // this line update for the data in database
        MessageSeen messageSeen = directConversation.getMessageSeenRecord().stream()
                .filter(ms -> ms.getUserId().equals(currentUser.getId()))
                .findFirst().orElse(null);
        // update the last seen message number of the user if the user has seen the conversation before
        if (messageSeen != null) {
            messageSeen.setLastSeenMessageNo(lastMessageNo);
        } else {
            // if there is no record of the user in the conversation, create a new record to check the seen message number of the user
            messageSeen = MessageSeen.builder()
//                    .id(UUID.randomUUID().toString())
                    .userId(currentUser.getId())
                    .lastSeenMessageNo(directConversation.getLastMessageNo())
                    .conversation(directConversation)
                    .build();
            directConversation.getMessageSeenRecord().add(messageSeen);
        }


    }
}
