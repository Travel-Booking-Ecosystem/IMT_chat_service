package com.imatalk.chatservice.service;


import com.imatalk.chatservice.dto.response.CommonResponse;
import com.imatalk.chatservice.dto.response.PeopleDTO;
import com.imatalk.chatservice.dto.response.UserDTO;
import com.imatalk.chatservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class SearchService {
    private final UserService userService;


    public ResponseEntity<?> searchPeople(User currentUser, String keyword) {
        log.info("Search people with keyword: {}", keyword);
        // if keyword is empty, return nothing
        List<User> people = new ArrayList();
        if (keyword.isEmpty()) {
            CommonResponse response = CommonResponse.success("Search user successfully",people);
            return ResponseEntity.ok(response);
        }

        // if keyword is not empty, return list of users that match the keyword
        if (keyword.startsWith("@")) {
            log.info("Search by username");
            // if keyword starts with @, return the user with that username
            people = userService.searchByUsernameStartsWith(keyword);
            log.info("Found {} users", people.size());
        } else if (isEmail(keyword)) {
            // if keyword is an email, return the user with that email
            log.info("Search by email");
            User user = userService.getUserBYEmail(keyword);
            if (user != null) {
                people.add(user);
            }
            log.info("Found {} users", people.size());
        } else {
            log.info("Search by display name");
            // otherwise, return the list of users that match the display name
            people = userService.searchByDisplayNameStartsWith(keyword);
            log.info("Found {} users", people.size());
        }

        // exclude the current user from the search result
        people.removeIf(user -> user.getId().equals(currentUser.getId()));

        log.info("Convert to DTO");
        List<PeopleDTO> searchResult = PeopleDTO.from(currentUser, people);
        log.info("Convert to DTO done");

        CommonResponse response = CommonResponse.success("Search user successfully", searchResult);
        return ResponseEntity.ok(response);
    }

    private boolean isEmail(String keyword) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "[a-zA-Z0-9-]+(?:\\."+
                "[a-zA-Z0-9-]+)*$";
        return keyword.matches(emailRegex);
    }

    public ResponseEntity<?> searchMessages(User currentUser, String keyword ) {
        return null;
    }
}
