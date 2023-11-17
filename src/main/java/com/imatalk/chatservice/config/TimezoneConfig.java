package com.imatalk.chatservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimezoneConfig {

    // config timezone 7
    @Bean
    public void setTimeZone() {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT+7"));
    }
}
