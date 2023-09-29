package com.imatalk.chatservice.service;


import com.imatalk.chatservice.entity.DirectConversation;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.exception.ApplicationException;
import com.imatalk.chatservice.repository.DirectConversationRepo;
import com.imatalk.chatservice.repository.MessageRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectConversationService {

    private final DirectConversationRepo directConversationRepo;
    private final MessageRepo messageRepo;


    public boolean checkIfConversationExistsBetween2Users(User user1, User user2) {
        return directConversationRepo.findByMembersIn(List.of(user1, user2)).isPresent();
    }

    public DirectConversation createAndSaveConversationBetween2Users(User user1, User user2) {
        DirectConversation conversation = DirectConversation.builder()
                .members(List.of(user1, user2))
                .lastMessageNo(0L)
                .lastMessageCreatedAt(null)
                .lastMessageId(null)
                .seenMessageTracker(DirectConversation.createDefaultSeenMessageTracker(List.of(user1, user2)))
                .build();

        return directConversationRepo.save(conversation);
    }

    public List<DirectConversation> getConversationListOfUser(User user, int numberOfConversations) {

        // get the first n conversation ids from the user's conversation list
        List<String> conversationIds = user.getDirectConversationInfoList().subList(0, numberOfConversations);

        // get a number of conversations from database
        List<DirectConversation> directConversations = directConversationRepo.findAllById(conversationIds);

        return directConversations;
    }

    public  User getTheOtherUserInConversation(User user, DirectConversation conversation) {

        // there can be only 2 members in a direct conversation
        // find the other user in the conversation
        User otherUser = conversation.getMembers().get(0);
        // if the first member is the current user, then the other user is the second member
        if (otherUser.getId().equals(user.getId())) {
            otherUser = conversation.getMembers().get(1);
        }
        return otherUser;
    }

    public DirectConversation getConversationById(String conversationId) {
        return directConversationRepo.findById(conversationId)
                .orElseThrow(() -> new ApplicationException("Conversation not found for id " + conversationId));
    }

    public void addMessageAndSaveConversation(DirectConversation directConversation, Message message) {
        directConversation.addMessage(message);
        directConversationRepo.save(directConversation);

    }


    public List<Message> get100Messages(DirectConversation directConversation, long messageNo) {
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
