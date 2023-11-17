package com.imatalk.chatservice.listener;

import com.imatalk.chatservice.event.FriendRequestAcceptedEvent;
import com.imatalk.chatservice.event.NewRegisteredUserEvent;
import com.imatalk.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppEventListener {

    private final String NEW_REGISTERED_USER_TOPIC = "new-registered-user";
    private final String FRIEND_REQUEST_ACCEPTED_TOPIC = "friend-request-accepted";

    private final ChatService chatService;
    @KafkaListener(topics = NEW_REGISTERED_USER_TOPIC, containerFactory = "appEventKafkaListenerContainerFactory")
    public void consumeNewRegisteredUserTopic(ConsumerRecord<String, NewRegisteredUserEvent> event) {
        log.info("New Registered User: " + event.value());
        chatService.createChatUser(event.value());
    }


    @KafkaListener(topics = FRIEND_REQUEST_ACCEPTED_TOPIC, containerFactory = "appEventKafkaListenerContainerFactory")
    public void consumeFriendRequestAcceptedTopic(ConsumerRecord<String, FriendRequestAcceptedEvent> event) {
        log.info("Friend Request Accepted: " + event.value());
        chatService.createDirectConversation(event.value());
    }
}
