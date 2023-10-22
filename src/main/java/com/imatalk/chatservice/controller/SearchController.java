package com.imatalk.chatservice.controller;

import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    // TODO: you will need to use ElasticSearch for this feature
    private final SearchService searchService;



    //TODO: don't use @RequestParam, use @RequestBody instead (for security reason)
    @GetMapping("/people")
    public ResponseEntity<?> searchPeople(@RequestParam String keyword) {
        log.info("CONTROLLER search people with keyword: {}", keyword);
        return searchService.searchPeople(getCurrentUser(),keyword);
    }

    //TODO: don't use @RequestParam, use @RequestBody instead (for security reason)

    @GetMapping("/messages")
    public ResponseEntity<?> searchMessages(@RequestParam String keyword) {
        return searchService.searchMessages(getCurrentUser() ,keyword);
    }


    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
