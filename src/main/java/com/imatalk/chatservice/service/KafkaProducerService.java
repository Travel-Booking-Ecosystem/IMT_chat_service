package com.imatalk.chatservice.service;

import com.imatalk.chatservice.dto.response.*;
import com.imatalk.chatservice.entity.ChatUser;
import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.event.GroupMessageRepliedEvent;
import com.imatalk.chatservice.event.NewConversationEvent;
import com.imatalk.chatservice.event.NewMessageEvent;
import com.imatalk.chatservice.event.NewMessageReactionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.imatalk.chatservice.dto.response.ConversationDetailsDTO.*;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    @Value("${topic.new-conversation}")
    private String NEW_CONVERSATION_TOPIC;

    @Value("${topic.new-message}")
    private String NEW_MESSAGE_TOPIC;

    @Value("${topic.group-message-replied}")
    private String GROUP_REPLIED_MESSAGE_TOPIC;

    @Value("${topic.new-message-reaction}")
    private String NEW_MESSAGE_REACTION_TOPIC;

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


    public void sendNewMessageEvent(MessageDTO messageDTO, List<String> memberIds) {


        NewMessageEvent.RepliedMessage repliedMessage = null;

        if (messageDTO.getRepliedMessage() != null) {
            repliedMessage = NewMessageEvent.RepliedMessage.builder()
                    .id(messageDTO.getRepliedMessage().getId())
                    .messageContent(messageDTO.getRepliedMessage().getMessageContent())
                    .senderName(messageDTO.getRepliedMessage().getSenderName())
                    .build();
        }



        // convert MessageDTO to NewMessageEvent.Message
        NewMessageEvent.Message message = NewMessageEvent.Message.builder()
                .id(messageDTO.getId())
                .senderId(messageDTO.getSenderId())
                .repliedMessage(repliedMessage)
                .messageNo(messageDTO.getMessageNo())
                .messageType(messageDTO.getMessageType())
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

    public void sendReactionMessageEvent(Message message, ConversationInfoDTO conversation, ChatUser reactor, List<String> memberIds, boolean isUnreact) {
        NewMessageReactionEvent.Conversation conversationDTO = NewMessageReactionEvent.Conversation.builder()
                .conversationId(message.getConversationId())
                .conversationName(conversation.getName())
                .conversationAvatar(conversation.getAvatar())
                .build();
        NewMessageReactionEvent.ReactionInformation reactionInformation = NewMessageReactionEvent.ReactionInformation.builder()
                .reactorId(reactor.getId())
                .reactorName(reactor.getDisplayName())
                .reaction(isUnreact ? null : message.getReactionTracker().get(reactor.getId()).getReaction())
                .reactedAt(isUnreact ? null : message.getReactionTracker().get(reactor.getId()).getReactedAt())
                .build();

        NewMessageReactionEvent.MessageReactionDTO messageReactionResponse = NewMessageReactionEvent.MessageReactionDTO.builder()
                .messageId(message.getId())
                .messageOwnerId(message.getSenderId())
                .conversation(conversationDTO)
                .reactionInformation(reactionInformation)
                .unReact(isUnreact)
                .build();

        NewMessageReactionEvent newMessageReactionEvent = NewMessageReactionEvent.builder()
                .conversationMemberIds(memberIds)
                .messageReaction(messageReactionResponse)
                .build();

        kafkaTemplate.send(NEW_MESSAGE_REACTION_TOPIC, newMessageReactionEvent);
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
