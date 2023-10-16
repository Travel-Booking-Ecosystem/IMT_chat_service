package com.imatalk.chatservice.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import static com.imatalk.chatservice.constants.ValidationRegex.*;

@Data
public class RegistrationRequest {
//    @Pattern(regexp = PASSWORD_REGEX,
//            message = PASSWORD_INVALID_ERROR)
    private String password;

    @Pattern(regexp = EMAIL_REGEX,
            message = EMAIL_INVALID_ERROR)
    private String email;

    @Pattern(regexp = DISPLAY_NAME_REGEX,
            message = DISPLAY_NAME_INVALID_ERROR)
    private String displayName;


    // if username is not provided, it will be generated from first name and last name
    // if it is provided, it can only contain letters, numbers, and underscores
    private String username; // optional

}
