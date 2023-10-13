package com.imatalk.chatservice.utils;

public class Utils {
    public static String generateAvatarUrl(String firstName) {
        return "https://ui-avatars.com/api/?name=" + firstName + "&background=" + randomColor();
    }

    private static String randomColor() {
        // generate 6 random hex digits
        int num = (int) (Math.random() * 0xFFFFFF);
        // force 6 hex digits, upper case too
        String color = String.format("%06X", num);
        return color;
    }
}
