package com.imatalk.chatservice.event;

import lombok.Data;

@Data
public class Event {
    private EventType type;
    private String userId;
    private EventName name;
    private Object payload;
}
