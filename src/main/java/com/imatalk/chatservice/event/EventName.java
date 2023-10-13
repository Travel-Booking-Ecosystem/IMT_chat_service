package com.imatalk.chatservice.event;

public enum EventName {
    // the first 3 types are used by the sidebar of the frontend
    // for event type CONVERSATION
    NEW_MESSAGE,
    NEW_CONVERSATION,

    // for event type FRIEND
    NEW_FRIEND_REQUEST,

    // for event type NOTIFICATION
    FRIEND_REQUEST_ACCEPTED,

    // this type is used by the chat box of the frontend where the user can chat with another user
    // for event type CHAT_BOX
    MESSAGE_SENT,
    MESSAGE_RECEIVED,
    MESSAGE_SEEN
}
