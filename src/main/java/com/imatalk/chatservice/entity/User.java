package com.imatalk.chatservice.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Document(collection = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class User implements UserDetails {
    private String id;
    // TODO: remove firstName and lastName, use displayName instead
    private String displayName;
    private String username; // this is unique, like @john_due21
    private String email;
    private String password;
    private String avatar;
    private LocalDateTime joinAt;
    private List<String> directConversationInfoList;
    private List<String> groupConversationInfoList;


    public void joinConversation(DirectConversation directConversation) {
        directConversationInfoList.add(directConversation.getId());
    }


    public List<String> getDirectConversationInfoList() {
        if (directConversationInfoList == null) {
            directConversationInfoList = Collections.emptyList();
        }
        return directConversationInfoList;
    }

    public List<String> getGroupConversationInfoList() {
        if (groupConversationInfoList == null) {
            groupConversationInfoList = Collections.emptyList();
        }
        return groupConversationInfoList;
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

    public void moveConversationToTop(DirectConversation directConversation) {
        directConversationInfoList.remove(directConversation.getId());
        directConversationInfoList.add(0, directConversation.getId());
    }
}
