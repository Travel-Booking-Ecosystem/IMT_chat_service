package com.imatalk.chatservice.dto.request;

import com.imatalk.chatservice.enums.MessageType;
import lombok.Data;

@Data
public class SendMessageRequest {
    private String conversationId;
    private String content;
    private String repliedMessageId;
    private String tempId;
    private MessageType messageType;
}
