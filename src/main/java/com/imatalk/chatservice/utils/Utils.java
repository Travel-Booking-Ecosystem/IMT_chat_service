package com.imatalk.chatservice.utils;

public class Utils {
    public static String generateAvatarUrl(String firstName) {
        return "https://ui-avatars.com/api/?name=" + firstName + "&background=random";
    }
}
