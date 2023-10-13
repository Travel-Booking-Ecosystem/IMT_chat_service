package com.imatalk.chatservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SidebarDTO {
    private List<ConversationInfoDTO> conversations;
    //TODO: please create DTO for the following objects
    private List<Object> friends;
    private List<Object> friendRequests;
    private List<Object> notifications;
}
