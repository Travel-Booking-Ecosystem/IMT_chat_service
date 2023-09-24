package com.imatalk.chatservice.dto.request;

import lombok.Data;

@Data
public class UpdateUserProfileRequest {
    private String firstName;
    private String lastName;
}
