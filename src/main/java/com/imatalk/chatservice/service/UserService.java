package com.imatalk.chatservice.service;


import com.imatalk.chatservice.dto.response.*;
import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.FriendRequest;
import com.imatalk.chatservice.entity.Notification;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.enums.ConversationStatus;
import com.imatalk.chatservice.event.Event;
import com.imatalk.chatservice.event.EventName;
import com.imatalk.chatservice.event.EventType;
import com.imatalk.chatservice.exception.ApplicationException;
import com.imatalk.chatservice.relationRepository.FriendRequestRepo;
import com.imatalk.chatservice.relationRepository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.imatalk.chatservice.event.EventName.NEW_FRIEND_REQUEST;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    // TODO: create Notification Service to send WS messages to the client
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final int NUMBER_OF_CONVERSATION_PER_REQUEST = 10; //TODO: move this to application.properties
    private final FriendRequestRepo friendRequestRepo;
    private final NotificationService notificationService;

    // TODO: create Notification Service to send WS messages to the client

    private final SimpMessagingTemplate messagingTemplate;
    @Value("${USER_TOPIC}")
    private String USER_TOPIC;


    public ResponseEntity<CommonResponse> getProfile(String currentUserId) {
        User currentUser = getUserById(currentUserId);
        // prepare the user profile
        UserDTO userProfile = new UserDTO(currentUser);
        CommonResponse response = CommonResponse.success("Profile retrieved", userProfile);
        return ResponseEntity.ok(response);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Load user by username {}", username);
        return userRepository.findByEmailIgnoreCase(username).orElseThrow(() -> new ApplicationException("User not found"));
    }

    public void saveAll(Iterable<User> users) {
        userRepository.saveAll(users);
    }

    public void save(User user) {
        userRepository.save(user);
    }


    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User getUserBYEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApplicationException("User not found for email: " + email));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApplicationException("Username not found for username: " + username));
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("User not found for id: " + id));
    }

    public List<User> findAllByIds(List<String> memberIds) {
        return userRepository.findAllByIdIn(memberIds);
    }

    public ResponseEntity<CommonResponse> getConversationList(String currentUserId) {
        User currentUser = getUserById(currentUserId);
        log.info("Get conversation list for user {}", currentUser.getUsername());
        ConversationListDTO sidebarDTO = new ConversationListDTO();
        sidebarDTO.setCurrentConversationId(currentUser.getCurrentConversationId());
        // fetch the user's recent conversations
        List<ConversationInfoDTO> conversationList = getRecentConversationInfo(currentUser);
        sidebarDTO.setConversations(conversationList);

        CommonResponse response = CommonResponse.success("Sidebar retrieved", sidebarDTO);
        return ResponseEntity.ok(response);
    }

    private List<ConversationInfoDTO> getRecentConversationInfo(User user) {
        // only get some recent conversations, if the user has too little conversations, get all of them
        int recentConversationNumber = Math.min(NUMBER_OF_CONVERSATION_PER_REQUEST, user.getConversations().size());

        List<Conversation> directConversations = conversationService.getConversationListOfUser(user, recentConversationNumber);

        List<ConversationInfoDTO> directConversationDTOs = directConversations.stream()
                .map(conversation -> new ConversationInfoDTO(conversation, user))
                .collect(Collectors.toList());

        return directConversationDTOs;
    }

    public List<User> searchByUsernameStartsWith(String keyword) {
        return userRepository.findAllByUsernameStartsWithIgnoreCase(keyword);
    }

    public User searchByEmail(String keyword) {
        return userRepository.findByEmailIgnoreCase(keyword).orElse(null);
    }

    public List<User> searchByDisplayNameStartsWith(String keyword) {
        return userRepository.findAllByDisplayNameStartsWithIgnoreCase(keyword);
    }

    public ResponseEntity<CommonResponse> addFriend(String currentUserId, String otherUserId) {
        User currentUser = getUserById(currentUserId);
        User otherUser = getUserById(otherUserId);


        // check if the 2 users are already friends
        if (currentUser.getFriends().contains(otherUser)) {
            throw new ApplicationException("You are already friends");
        }

        // check if there is already a friend request between the 2 users
        Optional<FriendRequest> user1RequestToUser2 = friendRequestRepo.findBySenderAndReceiver(currentUser, otherUser);
        if (user1RequestToUser2.isPresent()) {
            throw new ApplicationException("You have already sent a friend request to this user");
        }

        log.info("Create new friend request");
        FriendRequest friendRequest = FriendRequest
                .builder()
                .id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .sender(currentUser)
                .receiver(otherUser)
                .build();

        friendRequestRepo.save(friendRequest);


        // TODO: refactor this
        // send the request to the receiver
        Event event = Event
                .builder()
                .userId(otherUserId)
                .name(NEW_FRIEND_REQUEST)
                .payload(new FriendRequestDTO(friendRequest))
                .build();

        messagingTemplate.convertAndSend(USER_TOPIC + "/" + otherUserId, event);

        return ResponseEntity.ok(CommonResponse.success("Friend request sent"));
    }

    public ResponseEntity<CommonResponse> acceptFriend(String currentUserId, String requestId) {
        log.info("Accept friend request {}", requestId);
        User currentUser = getUserById(currentUserId);
        //TODO: move the code to FriendRequestService and ContactService
        FriendRequest friendRequest = friendRequestRepo.findById(requestId)
                .orElseThrow(() -> new ApplicationException("Friend request not found"));
        log.info("Friend request found");
        // check if the current user is the receiver of the friend request
        boolean isReceiver = friendRequest.getReceiver().getId().equals(currentUser.getId());
        if (!isReceiver) {
            throw new ApplicationException("You are not the receiver of this friend request");
        }

        // check if the friend request has already been accepted
        if (friendRequest.isAccepted()) {
            throw new ApplicationException("This friend request has already been accepted");
        }

//        friendRequestRepo.save(friendRequest);


        User sender = friendRequest.getSender();
        User receiver = friendRequest.getReceiver();
        makeTwoUsersFriend(sender, receiver);

        // create a conversation between 2 users
        createDirectConversationBetween2Users(sender, receiver);
        return ResponseEntity.ok(CommonResponse.success("Friend request accepted"));
    }

    private void createDirectConversationBetween2Users(User sender, User receiver) {
        List<User> members = List.of(sender, receiver);

        Conversation directConversation = Conversation.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .isGroupConversation(false)
                .members(members)
                .status(ConversationStatus.NEW) //
                .messageSeenRecord(new ArrayList<>())
                .build();


        conversationService.save(directConversation);
        for (User member : members) {
            member.joinConversation(directConversation);
        }
        userRepository.saveAll(members);


        // loop through each user to send notification about the new conversation
        for (User member : members) {
            log.info("Send notification to user {}", member.getUsername());
            // send notification to the user
            ConversationInfoDTO conversationInfoDTO = new ConversationInfoDTO(directConversation, member);

            Event event = Event.builder()
                    .userId(member.getId())
                    .type(EventType.CONVERSATION)
                    .name(EventName.NEW_CONVERSATION)
                    .payload(conversationInfoDTO)
                    .build();

            messagingTemplate.convertAndSend(USER_TOPIC + "/" + member.getId(), event);
        }

    }

    private void makeTwoUsersFriend(User sender, User receiver) {
        LocalDateTime time = LocalDateTime.now();
        // add 2 users to each other's friend list if they are not already friends
        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);

        // make all friend requests between the 2 users accepted
        Optional<FriendRequest> senderRequestToReceiver = friendRequestRepo.findBySenderAndReceiver(sender, receiver);
        Optional<FriendRequest> receiverRequestToSender = friendRequestRepo.findBySenderAndReceiver(receiver, sender);

        if (senderRequestToReceiver.isPresent()) {
            FriendRequest request = senderRequestToReceiver.get();
            request.setAccepted(true);
            friendRequestRepo.save(request);
        }

        if (receiverRequestToSender.isPresent()) {
            FriendRequest request = receiverRequestToSender.get();
            request.setAccepted(true);
            friendRequestRepo.save(request);
        }


        userRepository.saveAll(List.of(sender, receiver));


        //TODO: NOTIFICATION_SERVICE
        // send notification to the sender that the friend request has been accepted
        Notification notification = Notification.builder()
                .userId(sender.getId()) // the notification is sent to the sender
                .image(receiver.getAvatar())
                .title(receiver.getDisplayName())
                .content("Accepted your friend request")
                .createdAt(time)
                .unread(true)
                .build();

        notificationService.save(notification);


        Event event = Event.builder()
                .userId(sender.getId())
                .name(EventName.FRIEND_REQUEST_ACCEPTED)
                .payload(notification)
                .build();
        // send to the sender that the friend request has been accepted
        messagingTemplate.convertAndSend(USER_TOPIC + "/" + sender.getId(), event);


        for (User member : List.of(sender, receiver)) {
            User currentUser = member;
            User otherUser = member.equals(sender) ? receiver : sender;

            UserDTO newFriendDTO = new UserDTO(otherUser);
            Event newFriend = Event.builder()
                    .userId(currentUser.getId())
                    .name(EventName.NEW_FRIEND)
                    .payload(newFriendDTO) // send the info of the new friend to the user
                    .build();

            messagingTemplate.convertAndSend(USER_TOPIC + "/" + member.getId(), newFriend);
        }


    }

    public ResponseEntity<CommonResponse> getFriends(String currentUserId) {
        User currentUser = getUserById(currentUserId);
        List<UserDTO> friends = UserDTO.from(currentUser.getFriends());
        CommonResponse response = CommonResponse.success("Friends retrieved", friends);
        return ResponseEntity.ok(response);
    }


    public ResponseEntity<CommonResponse> getFriendRequest(String currentUserId) {
        User currentUser = getUserById(currentUserId);
//        User user = getUserById(currentUser.getId());
        // fetch all unaccepted received friend requests of the user
        List<FriendRequest> receivedFriendRequests = friendRequestRepo.findAllByReceiverAndIsAccepted(currentUser, false);
        List<FriendRequestDTO> friendRequests = FriendRequestDTO.from(receivedFriendRequests);
        CommonResponse response = CommonResponse.success("Friend requests retrieved", friendRequests);
        return ResponseEntity.ok(response);

    }

    public ResponseEntity<CommonResponse> getNotifications(String currentUserId) {
        return notificationService.getNotifications(currentUserId);
    }

    public ResponseEntity<CommonResponse> seeAllNotifications(String currentUserId) {

        return notificationService.seeAllNotifications(currentUserId);
    }
}
