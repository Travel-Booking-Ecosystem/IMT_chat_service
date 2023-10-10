package com.imatalk.chatservice.service;


import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.exception.ApplicationException;
import com.imatalk.chatservice.repository.DirectConversationRepo;
import com.imatalk.chatservice.repository.MessageRepo;
import com.imatalk.chatservice.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
//TODO: you will need to rename this class to ConversationService, since there is only one service for both direct and group conversation
//TODO: change the repositiory as well
public class DirectConversationService {

    private final DirectConversationRepo directConversationRepo;
    private final MessageRepo messageRepo;


    public boolean checkIfConversationExistsBetween2Users(User user1, User user2) {
        return directConversationRepo.findByMembersIn(List.of(user1, user2)).isPresent();
    }

    public Conversation createAndSaveConversationBetween2Users(User user1, User user2) {
        Conversation conversation = Conversation.builder()
                .members(List.of(user1, user2))
                .lastUpdatedAt(LocalDateTime.now())
                .seenMessageTracker(Conversation.createDefaultSeenMessageTracker(List.of(user1, user2)))
                .build();

        return directConversationRepo.save(conversation);
    }

    public List<Conversation> getConversationListOfUser(User user, int numberOfConversations) {

        // get the first n conversation ids from the user's conversation list
        List<String> conversationIds = user.getDirectConversationInfoList().subList(0, numberOfConversations);

        // get a number of conversations from database
        List<Conversation> directConversations = directConversationRepo.findAllByIdInOrderByLastUpdatedAtDesc(conversationIds);

        return directConversations;
    }

    public  User getTheOtherUserInConversation(User user, Conversation conversation) {

        // there can be only 2 members in a direct conversation
        // find the other user in the conversation
        User otherUser = conversation.getMembers().get(0);
        // if the first member is the current user, then the other user is the second member
        if (otherUser.getId().equals(user.getId())) {
            otherUser = conversation.getMembers().get(1);
        }
        return otherUser;
    }

    public Conversation getConversationById(String conversationId) {
        return directConversationRepo.findById(conversationId)
                .orElseThrow(() -> new ApplicationException("Conversation not found for id " + conversationId));
    }

    public void addMessageAndSaveConversation(Conversation directConversation, Message message) {
        directConversation.addMessage(message);
        directConversationRepo.save(directConversation);

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
        directConversationRepo.save(directConversation);
    }

    public Conversation createAndSaveGroupConversation(String groupName, List<User> members) {
        Conversation conversation = createEmptyConversation();

        // set users in the conversation
        conversation.setMembers(members);
        conversation.setGroupName(groupName);
        conversation.setGroupAvatar(Utils.generateAvatarUrl(groupName));
        conversation.setGroupConversation(true);

        return directConversationRepo.save(conversation);

    }


    private Conversation createEmptyConversation() {
        Conversation conversation = Conversation.builder()
                .members(List.of())
                .createdAt(LocalDateTime.now())
                .lastUpdatedAt(LocalDateTime.now())
                .seenMessageTracker(Conversation.createDefaultSeenMessageTracker(List.of()))
                .build();
        return conversation;
    }
}
