package com.imatalk.chatservice.service;

import com.imatalk.chatservice.dto.request.SendMessageRequest;
import com.imatalk.chatservice.entity.DirectConversation;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.repository.MessageRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class MessageService {
    private final MessageRepo messageRepo;


    public Message createAndSaveMessage(User user, SendMessageRequest request, DirectConversation directConversation) {
        // save the message to the database
        Message message = createMessage(user, request);

        // set the message to belong to the conversation
        message.setConversationId(directConversation.getId());
        message.setMessageNo(directConversation.getLastMessageNo() + 1);
        return messageRepo.save(message);
    }

    private Message createMessage(User currentUser, SendMessageRequest request) {
        LocalDateTime now = LocalDateTime.now();

        return Message.builder()
                .senderId(currentUser.getId())
                .content(request.getContent())
                .repliedMessageId(request.getRepliedMessageId())
                .createdAt(now)
                .build();
    }

    public List<Message> findAllByIds(List<String> messageIds) {
        return messageRepo.findAllById(messageIds);
    }
}
