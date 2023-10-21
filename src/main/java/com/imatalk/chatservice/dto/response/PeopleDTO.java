package com.imatalk.chatservice.dto.response;

import com.imatalk.chatservice.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class PeopleDTO {
    // used in SearchController when user search for people

    private String id;
    private String username; // this is unique, like @john_due21
    private String displayName;
    private String email;
    private String avatar;
    private boolean isRequestSent;
    private boolean isFriend;

    public PeopleDTO(User currentUser, User user) {
        // currentUser is the user who is searching for people
        // user is the user that is being searched
        this.id = user.getId();
        this.username = user.getUsername();
        this.displayName = user.getDisplayName();
        this.email = user.getEmail();
        this.avatar = user.getAvatar();
        this.isRequestSent = isRequestSent(currentUser, user);
        this.isFriend = isFriend(currentUser, user);
    }

    public static List<PeopleDTO> from(User currentUser, List<User> people) {
        return people.stream()
                .map(user -> new PeopleDTO(currentUser, user))
                .toList();
    }

    private static boolean isFriend(User currentUser, User user) {
        return currentUser.getFriends().stream()
                .anyMatch(friend -> friend.getId().equals(user.getId()));
    }

    private static boolean isRequestSent(User currentUser, User user) {
        return currentUser.getSentFriendRequests().stream()
                .anyMatch(request -> request.getReceiver().getId().equals(user.getId()));
    }
}
