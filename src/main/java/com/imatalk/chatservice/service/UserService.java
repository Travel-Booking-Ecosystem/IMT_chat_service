package com.imatalk.chatservice.service;


import com.imatalk.chatservice.dto.response.CommonResponse;
import com.imatalk.chatservice.dto.response.ConversationInfoDTO;
import com.imatalk.chatservice.dto.response.SidebarDTO;
import com.imatalk.chatservice.dto.response.UserProfileDTO;
import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.exception.ApplicationException;
import com.imatalk.chatservice.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepo userRepo;
    private final ConversationService conversationService;
    private final int NUMBER_OF_CONVERSATION_PER_REQUEST = 10; //TODO: move this to application.properties


    public ResponseEntity<CommonResponse> getProfile(User user) {
        // prepare the user profile
        UserProfileDTO userProfile = new UserProfileDTO(user);
        CommonResponse response = CommonResponse.success("Profile retrieved", userProfile);
        return ResponseEntity.ok(response);
    }




    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findById(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
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
        return userRepo.findByEmail(email)
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

    public ResponseEntity<CommonResponse> getSidebar(User currentUser) {
        SidebarDTO sidebarDTO = new SidebarDTO();
        // fetch the user's recent conversations
        List<ConversationInfoDTO> conversationList = getRecentDirectConversationInfo(currentUser);
        sidebarDTO.setConversations(conversationList);

        //TODO: please implement the following
        // fetch the user's friends
        // fetch the user's friend requests
        // fetch the user's notifications
        CommonResponse response = CommonResponse.success("Sidebar retrieved", sidebarDTO);
        return ResponseEntity.ok(response);
    }

    private List<ConversationInfoDTO> getRecentDirectConversationInfo(User user) {
        // only get some recent conversations, if the user has too little conversations, get all of them
        int recentConversationNumber = Math.min(NUMBER_OF_CONVERSATION_PER_REQUEST, user.getDirectConversationInfoList().size());
        List<Conversation> directConversations = conversationService.getConversationListOfUser(user, recentConversationNumber);

        List<ConversationInfoDTO> directConversationDTOs = directConversations.stream()
                .map(conversation -> new ConversationInfoDTO(conversation, user))
                .collect(Collectors.toList());

        return directConversationDTOs;
    }
}
