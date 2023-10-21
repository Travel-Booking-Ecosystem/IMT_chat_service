package com.imatalk.chatservice.service;


import com.imatalk.chatservice.dto.response.*;
import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.FriendRequest;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.exception.ApplicationException;
import com.imatalk.chatservice.repository.FriendRequestRepo;
import com.imatalk.chatservice.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepo userRepo;
    private final ConversationService conversationService;
    private final int NUMBER_OF_CONVERSATION_PER_REQUEST = 10; //TODO: move this to application.properties
    private final FriendRequestRepo friendRequestRepo;

    public ResponseEntity<CommonResponse> getProfile(User user) {
        // prepare the user profile
        UserDTO userProfile = new UserDTO(user);
        CommonResponse response = CommonResponse.success("Profile retrieved", userProfile);
        return ResponseEntity.ok(response);
    }




    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findById(username).orElseThrow(() -> new ApplicationException("User not found"));
    }
    public void saveAll(Iterable<User> users) {
        userRepo.saveAll(users);
    }

    public void save(User user) {
        userRepo.save(user);
    }


    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    public User getUserBYEmail(String email) {
        return userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApplicationException("User not found for email: " + email));
    }

    public User getUserByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new ApplicationException("Username not found for username: " + username));
    }

    public User getUserById(String id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ApplicationException("User not found for id: " + id));
    }

    public List<User> findAllByIds(List<String> memberIds) {
        return userRepo.findAllByIdIn(memberIds);
    }

    public ResponseEntity<CommonResponse> getConversationList(User currentUser) {
        ConversationListDTO sidebarDTO = new ConversationListDTO();
        sidebarDTO.setCurrentConversationId(currentUser.getCurrentConversationId());
        // fetch the user's recent conversations
        List<ConversationInfoDTO> conversationList = getRecentDirectConversationInfo(currentUser);
        sidebarDTO.setConversations(conversationList);

        CommonResponse response = CommonResponse.success("Sidebar retrieved", sidebarDTO);
        return ResponseEntity.ok(response);
    }

    private List<ConversationInfoDTO> getRecentDirectConversationInfo(User user) {
        // only get some recent conversations, if the user has too little conversations, get all of them
        int recentConversationNumber = Math.min(NUMBER_OF_CONVERSATION_PER_REQUEST, user.getConversations().size());
        List<Conversation> directConversations = conversationService.getConversationListOfUser(user, recentConversationNumber);

        List<ConversationInfoDTO> directConversationDTOs = directConversations.stream()
                .map(conversation -> new ConversationInfoDTO(conversation, user))
                .collect(Collectors.toList());

        return directConversationDTOs;
    }

    public List<User> searchByUsernameStartsWith(String keyword) {
        return userRepo.findAllByUsernameStartsWithIgnoreCase(keyword);
    }

    public User searchByEmail(String keyword) {
        return userRepo.findByEmailIgnoreCase(keyword).orElse(null);
    }

    public List<User> searchByDisplayNameStartsWith(String keyword) {
        return userRepo.findAllByDisplayNameStartsWithIgnoreCase(keyword);
    }

    public ResponseEntity<CommonResponse> addFriend(User currentUser, String otherUserId) {
        //TODO move the code to the ContactService
        User otherUser = getUserById(otherUserId);

        boolean alreadyFriend = currentUser.getFriends().stream()
                .anyMatch(friend -> friend.getId().equals(otherUserId));

        if (alreadyFriend) {
            throw new ApplicationException("You are already friend with this user");
        }


        boolean alreadySentRequest = currentUser.getSentFriendRequests().stream()
                .anyMatch(friendRequest -> friendRequest.getReceiver().getId().equals(otherUserId));

        if (alreadySentRequest) {
            throw new ApplicationException("You have already sent a friend request to this user");
        }


        boolean receiverHasSentOtherRequest = currentUser.getReceivedFriendRequests().stream()
                .anyMatch(friendRequest -> friendRequest.getSender().getId().equals(otherUserId));


        // if the other user has already sent a friend request to the current user,
        // and the current user is sending the friend request to that other user
        // it means that the current user is accepting the friend request
        if (receiverHasSentOtherRequest) {
            FriendRequest friendRequest = currentUser.getReceivedFriendRequests().stream()
                    .filter(request -> request.getSender().getId().equals(otherUserId))
                    .findFirst()
                    .orElseThrow(() -> new ApplicationException("Friend request not found"));

            acceptFriend(currentUser, friendRequest.getId());

            return ResponseEntity.ok(CommonResponse.success("Add friend successfully"));

        } else {
            FriendRequest friendRequest = FriendRequest
                    .builder()
                    .createdAt(LocalDateTime.now())
                    .receiver(otherUser)
                    .sender(currentUser)
                            .build();

            friendRequestRepo.save(friendRequest);
            // add the friend request to the current user's friend request list and the other user's friend request list
            otherUser.getReceivedFriendRequests().add(friendRequest);
            currentUser.getSentFriendRequests().add(friendRequest);
            userRepo.saveAll(List.of(currentUser, otherUser));

        }
        return ResponseEntity.ok(CommonResponse.success("Friend request sent"));
    }

    public ResponseEntity<CommonResponse> acceptFriend(User currentUser, String requestId) {
        //TODO: move the code to FriendRequestService and ContactService
        FriendRequest friendRequest = friendRequestRepo.findById(requestId)
                .orElseThrow(() -> new ApplicationException("Friend request not found"));

        // check if the current user is the receiver of the friend request
        boolean isReceiver = friendRequest.getReceiver().getId().equals(currentUser.getId());
        if (!isReceiver) {
            throw new ApplicationException("You are not the receiver of this friend request");
        }

        if (friendRequest.isAccepted()) {
            throw new ApplicationException("This friend request has already been accepted");
        }

        friendRequest.setAccepted(true);
        friendRequestRepo.save(friendRequest);


        User sender = friendRequest.getSender();
        User receiver = friendRequest.getReceiver();
        makeTwoUsersFriend(sender, receiver, friendRequest);

        userRepo.saveAll(List.of(sender, receiver));
        return ResponseEntity.ok(CommonResponse.success("Friend request accepted"));
    }

    private void makeTwoUsersFriend(User sender, User receiver, FriendRequest friendRequest) {

        // add 2 users to each other's friend list and
        // remove the friend request from the receiver's friend request list and the sender's friend request list
        sender.getFriends().add(receiver);
        sender.getReceivedFriendRequests().removeIf(request -> request.getId().equals(friendRequest.getId()));

        receiver.getFriends().add(sender);
        receiver.getSentFriendRequests().removeIf(request -> request.getId().equals(friendRequest.getId()));

    }

    public ResponseEntity<CommonResponse> getFriends(User currentUser) {
        List<UserDTO> friends = UserDTO.from(currentUser.getFriends());
        CommonResponse response = CommonResponse.success("Friends retrieved", friends);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<CommonResponse> getFriendRequests(User currentUser) {
        //TODO: considering create a Contact service to handle friend request
        List<FriendRequestDTO> friendRequests = FriendRequestDTO.from(currentUser.getReceivedFriendRequests());
        CommonResponse response = CommonResponse.success("Friend requests retrieved", friendRequests);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<CommonResponse> getFriendRequest(User currentUser) {


        List<FriendRequestDTO> friendRequests = FriendRequestDTO.from(currentUser.getReceivedFriendRequests());
        CommonResponse response = CommonResponse.success("Friend requests retrieved", friendRequests);
        return ResponseEntity.ok(response);

    }
}
