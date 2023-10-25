package com.imatalk.chatservice.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ToString(exclude = {"conversations", "friends"}) // to prevent infinite loop when toString() is called
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_conversation", // Name of the join table
            joinColumns = @JoinColumn(name = "user_id"), // Foreign key column in the join table for User
            inverseJoinColumns = @JoinColumn(name = "conversation_id") // Foreign key column in the join table for Conversation
    )
    @JsonIgnore
    private List<Conversation> conversations;
    //TODO: create a list of new friend request for this user (one more field or for the DTO only)

//    @JsonIgnore
//    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY)
//    private List<FriendRequest> receivedFriendRequests;
//
//    @JsonIgnore
//    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY)
//    private List<FriendRequest> sentFriendRequests;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> friends;

    public void joinConversation(Conversation conversation) {
        conversations.add(conversation);
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

    public List<Conversation> getConversations() {
        if (conversations == null) {
            conversations = new ArrayList<>();
        }
        return conversations;
    }

//    public List<FriendRequest> getReceivedFriendRequests() {
//        if (receivedFriendRequests == null) {
//            receivedFriendRequests = new ArrayList<>();
//        }
//        return receivedFriendRequests;
//    }
//
//    public List<FriendRequest> getSentFriendRequests() {
//        if (sentFriendRequests == null) {
//            sentFriendRequests = new ArrayList<>();
//        }
//        return sentFriendRequests;
//    }

    public List<User> getFriends() {
        if (friends == null) {
            friends = new ArrayList<>();
        }
        return friends;
    }
}
