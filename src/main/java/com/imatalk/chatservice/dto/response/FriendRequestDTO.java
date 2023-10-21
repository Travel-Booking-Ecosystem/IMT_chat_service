package com.imatalk.chatservice.dto.response;

import com.imatalk.chatservice.entity.FriendRequest;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FriendRequestDTO {
    private String id;
    private UserDTO sender;
    private LocalDateTime createdAt;
    private boolean isAccepted;

    public FriendRequestDTO(FriendRequest friendRequest) {
        this.id = friendRequest.getId();
        this.sender = new UserDTO(friendRequest.getSender());
        this.createdAt = friendRequest.getCreatedAt();
        this.isAccepted = friendRequest.isAccepted();
    }

    public static List<FriendRequestDTO> from(List<FriendRequest> friendRequests) {
        return friendRequests.stream()
                .map(FriendRequestDTO::new)
                .toList();
    }
}
