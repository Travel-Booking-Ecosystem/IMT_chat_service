package com.imatalk.chatservice.dto.response;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class UserProfile {
    private String id;
    private String username; // this is unique, like @john_due21
    private String displayName;
    private String email;
    private String avatar;




}



