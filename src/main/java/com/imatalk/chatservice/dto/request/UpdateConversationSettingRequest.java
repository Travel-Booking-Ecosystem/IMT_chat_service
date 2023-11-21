package com.imatalk.chatservice.dto.request;

import lombok.Data;

@Data
public class UpdateConversationSettingRequest {
    private String conversationId;
    private String themeColor;
    private String wallpaper;
    private String defaultReaction;
}
