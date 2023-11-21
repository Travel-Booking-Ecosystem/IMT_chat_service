package com.imatalk.chatservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendMessageResponse {
    private boolean success;
    private String id; // this is the id the message in the database
    private String tempId; // this is the id that client sent to server (the client will use this id to update the message status)
    private long messageNo;
    private LocalDateTime createdAt;
}
