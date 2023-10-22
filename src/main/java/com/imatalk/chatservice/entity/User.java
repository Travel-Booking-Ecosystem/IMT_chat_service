package com.imatalk.chatservice.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Document(collection = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ToString(exclude = {"conversations", "receivedFriendRequests", "sentFriendRequests", "friends"}) // to prevent infinite loop when toString() is called
public class User implements UserDetails {
    @Id
    private String id;
    // TODO: remove firstName and lastName, use displayName instead
    private String displayName;
    private String username; // this is unique, like @john_due21
    private String email;
    private String password;
    private String avatar;
    private LocalDateTime joinAt;
    private String currentConversationId; // the id of the conversation that the user is currently in
    @DBRef
    private List<Conversation> conversations;
    //TODO: create a list of new friend request for this user (one more field or for the DTO only)
    @DBRef
    private List<FriendRequest> receivedFriendRequests;
    @DBRef
    private List<FriendRequest> sentFriendRequests;

    //TODO: List<User> friends
    @DBRef
    private List<User> friends;

    public void joinConversation(Conversation conversation) {
        conversations.add(conversation);
    }

    public List<Conversation> getConversations() {
        if (conversations == null) {
            conversations = new ArrayList<>();
        }
        return conversations;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton((GrantedAuthority) () -> "ROLE_USER");
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }


    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    public List<FriendRequest> getReceivedFriendRequests() {
        if (receivedFriendRequests == null) {
            receivedFriendRequests = new ArrayList<>();
        }
        return receivedFriendRequests;
    }

    public List<FriendRequest> getSentFriendRequests() {
        if (sentFriendRequests == null) {
            sentFriendRequests = new ArrayList<>();
        }
        return sentFriendRequests;
    }

    public List<User> getFriends() {
        if (friends == null) {
            friends = new ArrayList<>();
        }
        return friends;
    }
}
