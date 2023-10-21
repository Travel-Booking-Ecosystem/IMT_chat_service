package com.imatalk.chatservice.controller;

import org.springframework.web.bind.annotation.RestController;

@RestController("/api/contact")
public class ContactController {

    // reject friend request

    // TODO: when a user reject a friend request, the other user should be notified, also set the isAccepted to false for all friend request between the two users
}
