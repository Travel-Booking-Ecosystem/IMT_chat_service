package com.imatalk.chatservice.dto.request;

import com.imatalk.chatservice.enums.MessageReaction;
import lombok.Data;

@Data
public class ReactMessageRequest {
    private String conversationId;
    private String messageId;
    private String reactorId;
    private MessageReaction reaction;
}
