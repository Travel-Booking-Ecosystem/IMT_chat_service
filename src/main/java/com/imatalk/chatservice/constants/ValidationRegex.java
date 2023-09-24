package com.imatalk.chatservice.constants;

public class ValidationRegex {
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String EMAIL_INVALID_ERROR = "Email is invalid";

    // password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    public static final String PASSWORD_INVALID_ERROR = "Password must contain at least 8 characters, one uppercase, one lowercase, one number and one special character";

    // name must be at least 2 characters long, contain only letters, and not contain any special characters
    public static final String NAME_REGEX = "^[a-zA-Z]{2,}$";
    public static final String NAME_INVALID_ERROR = "Name must contain at least 2 characters, and only letters";

    // username must be at least 5 characters long, contain only lowercase letters, numbers, and underscores, or not provided
    public static final String USERNAME_REGEX = "^[a-z0-9_]{5,}$";
    public static final String USERNAME_INVALID_ERROR = "Username must contain at least 5 characters, and only lowercase letters, numbers, and underscores";
}
