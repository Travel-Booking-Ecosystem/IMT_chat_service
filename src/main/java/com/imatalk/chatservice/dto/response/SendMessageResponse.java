package com.imatalk.chatservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SendMessageResponse {
    private boolean success;
    private LocalDateTime sentAt;
}
