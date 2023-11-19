package com.imatalk.chatservice.service;

import com.imatalk.chatservice.dto.response.*;
import com.imatalk.chatservice.entity.ChatUser;
import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.event.GroupMessageRepliedEvent;
import com.imatalk.chatservice.event.NewConversationEvent;
import com.imatalk.chatservice.event.NewMessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.imatalk.chatservice.dto.response.ConversationChatHistoryDTO.*;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    @Value("${topic.new-conversation}")
    private String NEW_CONVERSATION_TOPIC;

    @Value("${topic.new-message}")
    private String NEW_MESSAGE_TOPIC;

    @Value("${topic.group-message-replied}")
    private String GROUP_REPLIED_MESSAGE_TOPIC;


    private final KafkaTemplate<String, Object> kafkaTemplate;


    public void sendNewConversationEvent(ConversationInfoDTO conversationInfoDTO, String memberId) {

            // convert ConversationInfoDTO.LastMessage to NewConversationEvent.LastMessage
            NewConversationEvent.LastMessage lastMessage = NewConversationEvent.LastMessage.builder()
                    .id(conversationInfoDTO.getLastMessage().getId())
                    .content(conversationInfoDTO.getLastMessage().getContent())
                    .createdAt(conversationInfoDTO.getLastMessage().getCreatedAt())
                    .build();

            // convert ConversationInfoDTO to NewConversationEvent.ConversationInfo
            NewConversationEvent.ConversationInfo conversationInfo = NewConversationEvent.ConversationInfo.builder()
                    .id(conversationInfoDTO.getId())
                    .name(conversationInfoDTO.getName())
                    .avatar(conversationInfoDTO.getAvatar())
                    .lastMessage(lastMessage)
                    .unread(conversationInfoDTO.isUnread())
                    .status(conversationInfoDTO.getStatus())
                    .lastUpdate(conversationInfoDTO.getLastUpdate())
                    .build();


            NewConversationEvent newConversationEvent = NewConversationEvent.builder()
                    .conversationInfo(conversationInfo)
                    .userId(memberId)
                    .build();

            kafkaTemplate.send(NEW_CONVERSATION_TOPIC, newConversationEvent);
    }

//    public void sendNewFriendRequestEvent(FriendRequestDTO friendRequestDTO, String receiverId) {
//
//
//        // Convert FriendRequestDTO.User to NewFriendRequestEvent.User
//        UserProfile requestSender = friendRequestDTO.getSender();
//        NewFriendRequestEvent.User sender = NewFriendRequestEvent.User.builder()
//                .id(requestSender.getId())
//                .username(friendRequestDTO.getSender().getUsername())
//                .displayName(friendRequestDTO.getSender().getDisplayName())
//                .email(friendRequestDTO.getSender().getEmail())
//                .avatar(friendRequestDTO.getSender().getAvatar())
//                .joinAt(friendRequestDTO.getSender().getJoinAt())
//                .build();
//
//
//        // convert FriendRequestDTO to NewFriendRequestEvent.FriendRequest
//        NewFriendRequestEvent.FriendRequest friendRequest = NewFriendRequestEvent.FriendRequest.builder()
//                .id(friendRequestDTO.getId())
//                .sender(sender)
//                .createdAt(friendRequestDTO.getCreatedAt())
//                .isAccepted(friendRequestDTO.isAccepted())
//                .build();
//
//
//        NewFriendRequestEvent newFriendRequestEvent = NewFriendRequestEvent.builder()
//                .friendRequest(friendRequest)
//                .receiverId(receiverId)
//                .build();
//
//        kafkaTemplate.send(newFriendRequestTopic, newFriendRequestEvent);
//    }
//
//    public void sendNewNotification(NotificationDTO notificationDTO) {
//
//        // convert NotificationDTO to NewNotificationEvent.Notification
//        NewNotificationEvent.Notification notification = NewNotificationEvent.Notification.builder()
//                .id(notificationDTO.getId())
//                .userId(notificationDTO.getUserId())
//                .image(notificationDTO.getImage())
//                .title(notificationDTO.getTitle())
//                .content(notificationDTO.getContent())
//                .unread(notificationDTO.isUnread())
//                .createdAt(notificationDTO.getCreatedAt())
//                .build();
//
//        NewNotificationEvent newNotificationEvent = NewNotificationEvent.builder()
//                .userId(notificationDTO.getUserId())
//                .notification(notification)
//                .build();
//
//        kafkaTemplate.send(newNotificationTopic, newNotificationEvent);
//    }

    public void sendNewMessageEvent(MessageDTO messageDTO, List<String> memberIds) {
        // convert MessageDTO to NewMessageEvent.Message
        NewMessageEvent.Message message = NewMessageEvent.Message.builder()
                .id(messageDTO.getId())
                .senderId(messageDTO.getSenderId())
                .repliedMessageId(messageDTO.getRepliedMessageId())
                .messageNo(messageDTO.getMessageNo())
                .conversationId(messageDTO.getConversationId())
                .content(messageDTO.getContent())
                .createdAt(messageDTO.getCreatedAt())
                .build();

        NewMessageEvent newMessageEvent = NewMessageEvent.builder()
                .conversationMemberIds(memberIds)
                .message(message)
                .build();

        kafkaTemplate.send(NEW_MESSAGE_TOPIC, newMessageEvent);
    }

    public void sendGroupMessageRepliedEvent(Message repliedMessage, Conversation conversation, ChatUser replier) {



        GroupMessageRepliedEvent.RepliedMessage message = GroupMessageRepliedEvent.RepliedMessage.builder()
                .id(repliedMessage.getId())
                .senderId(repliedMessage.getSenderId())
                .content(repliedMessage.getContent())
                .createdAt(repliedMessage.getCreatedAt())
                .conversationId(repliedMessage.getConversationId())
                .messageNo(repliedMessage.getMessageNo())
                .build();

        GroupMessageRepliedEvent.Conversation conversationInfo = GroupMessageRepliedEvent.Conversation.builder()
                .id(conversation.getId())
                .name(conversation.getGroupName())
                .avatar(conversation.getGroupAvatar())
                .build();

        GroupMessageRepliedEvent.Replier replierInfo = GroupMessageRepliedEvent.Replier.builder()
                .id(replier.getId())
                .username(replier.getUsername())
                .displayName(replier.getDisplayName())
                .avatar(replier.getAvatar())
                .build();

        GroupMessageRepliedEvent groupMessageRepliedEvent = GroupMessageRepliedEvent.builder()
                .conversation(conversationInfo)
                .message(message)
                .replier(replierInfo)
                .build();

        kafkaTemplate.send(GROUP_REPLIED_MESSAGE_TOPIC, groupMessageRepliedEvent);
    }
}
