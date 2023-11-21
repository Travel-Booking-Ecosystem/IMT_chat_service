package com.imatalk.chatservice.service;

import com.imatalk.chatservice.dto.request.ReactMessageRequest;
import com.imatalk.chatservice.dto.request.SendMessageRequest;
import com.imatalk.chatservice.entity.ChatUser;
import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.mongoRepository.MessageRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class MessageService {
    private final MessageRepo messageRepo;

    //TOOD: rename all "directConversation" to "conversation"
    public Message createAndSaveMessage(ChatUser user, SendMessageRequest request, Conversation conversation) {
        // save the message to the database
        Message message = createMessage(user, request);

        // set the message to belong to the conversation
        message.setConversationId(conversation.getId());
        long newMessageNo = conversation.getLastMessageNo() + 1;
        message.setMessageNo(newMessageNo);
        return messageRepo.save(message);
    }

    private Message createMessage(ChatUser currentUser, SendMessageRequest request) {
        LocalDateTime now = LocalDateTime.now();

        return Message.builder()
                .senderId(currentUser.getId())
                .messageType(request.getMessageType())
                .content(request.getContent())
                .repliedMessageId(request.getRepliedMessageId())
                .createdAt(now)
                .build();
    }

    public Message findMessageById(String messageId) {
        return messageRepo.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found for id " + messageId));
    }

    public List<Message> findAllByIds(List<String> messageIds) {
        return messageRepo.findAllById(messageIds);
    }

    public Map<String, Object> reactMessage(Message message, ReactMessageRequest request) {
        boolean isUnreact = false;
        Map<String, Message.Reactor> reactionTracker = message.getReactionTracker();
        if (reactionTracker == null) {
            reactionTracker = new HashMap<>();
        }

        if (reactionTracker.containsKey(request.getReactorId())) {
            if (reactionTracker.get(request.getReactorId()).getReaction() == request.getReaction()) {
                // remove the reaction
                reactionTracker.remove(request.getReactorId());
                isUnreact = true;
            } else {
                // update the reaction
                reactionTracker.get(request.getReactorId()).setReaction(request.getReaction());
                reactionTracker.get(request.getReactorId()).setReactedAt(LocalDateTime.now());
            }
        } else {
            // add the new reaction
            Message.Reactor reactor = Message.Reactor.builder()
                    .reaction(request.getReaction())
                    .reactedAt(LocalDateTime.now())
                    .build();
            reactionTracker.put(request.getReactorId(), reactor);
        }

        message.setReactionTracker(reactionTracker);
        messageRepo.save(message);
        return Map.of("isUnreact", isUnreact, "message", message);
    }
}
