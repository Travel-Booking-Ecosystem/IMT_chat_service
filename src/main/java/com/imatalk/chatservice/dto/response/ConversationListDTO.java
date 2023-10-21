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
public class ConversationListDTO {
    private String currentConversationId; //TODO: remove this
    private List<ConversationInfoDTO> conversations;

//    private List<Object> notifications; // TODO: // remove this
}
