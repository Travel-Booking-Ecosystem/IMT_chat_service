package com.imatalk.chatservice.dto.response;

import com.imatalk.chatservice.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
public class UserDTO {
    private String id;
    private String username; // this is unique, like @john_due21
    private String displayName;
    private String email;
    private String avatar;
    private LocalDateTime joinAt;


    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.displayName = user.getDisplayName();
        this.email = user.getEmail();
        this.avatar = user.getAvatar();
        this.joinAt = user.getJoinAt();
    }


    public static List<UserDTO> from(List<User> users) {
        if (users == null) return List.of(); //TODO: remove this (just for testing
        return users.stream()
                .map(UserDTO::new)
                .toList();
    }

}



