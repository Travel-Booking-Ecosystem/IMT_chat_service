package com.imatalk.chatservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "conversations")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ToString(exclude = {"messages"}) // to prevent the call to getMessages() method when toString() is called
public class Conversation {
    private String id;
    private LocalDateTime createdAt;
    @DBRef
    private List<User> members;

    private Message lastMessage; // this is the last message of the conversation used to display in the conversation list
    private LocalDateTime lastUpdatedAt; // this field is used to sort the conversation list when user opens the app
    @DBRef
    private List<Message> messages;
    private Map<String, Long> seenMessageTracker; // track each user's last seen message number in this conversation

    private boolean isGroupConversation;
    private String groupName;
    private String groupAvatar;

    // make this method private to prevent it from being exposed to the outside
    // when this method is called, the messages field will be populated by retrieving messages from the database
    // this is done by spring data mongodb and doing it can be expensive and unoptimized!
    // the messages should only be fetched using its own repository
    @JsonIgnore
    private List<Message> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        return messages;
    }

    public List<User> getMembers() {
        if (members == null) {
            members = new ArrayList<>();
        }
        return members;
    }

    public Map<String, Long> getSeenMessageTracker() {
        if (seenMessageTracker == null) {
            seenMessageTracker = new HashMap<>();
        }
        return seenMessageTracker;
    }

    public static Map<String, Long> createDefaultSeenMessageTracker(List<User> users) {
        Map<String, Long> map = new HashMap<String, Long>();

        // by default, all members have seen message number 0 (no message seen)
        for (User member : users) {
            map.put(member.getId(), 0L);
        }

        return map;
    }


    public void addMessage(Message message) {
        // set messageNo to be the next number in the sequence
        long lastMessageNo = lastMessage != null ? lastMessage.getMessageNo() : 0L;
        long newMessageNo = lastMessageNo + 1;
        messages.add(message);

        // update the seen message number for the sender of the message
        for (User member : members) {
            // if the user is the sender of the message, set the seen message number to be the message number of the message
            // otherwise, set the seen message number to be the last seen message number of the user
            long seenMessageNo = member.getId().equals(message.getSenderId()) ? newMessageNo : seenMessageTracker.getOrDefault(member.getId(), 0L);
            seenMessageTracker.put(member.getId(), seenMessageNo);

        }

        this.lastMessage = message;
        lastUpdatedAt = message.getCreatedAt();
    }


    public Long getLastSeenMessageNoOfMember(String memberId) {
        return seenMessageTracker.getOrDefault(memberId, 0L);
    }

    public void updateUserSeenLatestMessage(User currentUser) {
        long lastMessageNo = lastMessage != null ? lastMessage.getMessageNo() : 0L;
        seenMessageTracker.put(currentUser.getId(), lastMessageNo);
    }

    public long getLastMessageNo() {
        return lastMessage != null ? lastMessage.getMessageNo() : 0L;
    }
}
