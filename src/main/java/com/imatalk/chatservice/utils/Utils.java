package com.imatalk.chatservice.utils;

public class Utils {
    public static String generateAvatarUrl(String firstName) {
        return "https://ui-avatars.com/api/?name=" + firstName + "&background=" + randomColor();
    }

    private static String randomColor() {
        // generate 6 random hex digits
        // i choose these hex colors as background color for the avatar  because they are beautiful
        // and they look nice with the color black of the text
        String[] hexColors = {
                "CDF5FD",
                "A0E9FF",
                "00A9FF",
                "F875AA",
                "FFDFDF",
                "FFF6F6",
                "AEDEFC",
                "F5F7F8",
                "F4CE14",
                "ED7D31",
                "ED7D31",
                "940B92",
                "DA0C81",
                "B6FFFA",
                "FAF2D3",
                "FFCC70",
                "45FFCA",
                "279EFF",
                "40F8FF",
                "F8DE22",
                "F94C10",
                "E8FFCE",
                "1D5D9B"
        };

        return hexColors[(int) (Math.random() * hexColors.length)];
    }
}
