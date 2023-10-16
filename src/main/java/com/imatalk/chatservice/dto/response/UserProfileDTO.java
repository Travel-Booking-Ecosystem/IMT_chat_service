package com.imatalk.chatservice.dto.response;

import com.imatalk.chatservice.entity.User;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class UserProfileDTO {
    private String id;
    private String username; // this is unique, like @john_due21
    private String displayName;
    private String email;
    private String avatar;
    private LocalDateTime joinAt;
    private String currentConversationId;

    public UserProfileDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.displayName = user.getDisplayName();
        this.email = user.getEmail();
        this.avatar = user.getAvatar();
        this.joinAt = user.getJoinAt();
        this.currentConversationId = user.getCurrentConversationId();
    }


}



