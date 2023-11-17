package com.imatalk.chatservice.config;
//TODO: KAFKA PRODUCER CONFIG

import com.imatalk.chatservice.event.*;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${topic.friend-request-accepted}")
    private String FRIEND_REQUEST_ACCEPTED_TOPIC;


    @Value("${topic.new-registered-user}")
    private String NEW_REGISTERED_USER_TOPIC;

    @Value("${topic.new-message}")
    private String NEW_MESSAGE_TOPIC;


    @Value("${topic.new-conversation}")
    private String NEW_CONVERSATION_TOPIC;


    @Value("${topic.group-message-replied}")
    private String GROUP_MESSAGE_REPLIED_TOPIC;



    //TODO: NEW_FRIEND

    @Bean
    public NewTopic newConversationTopic() {
        return TopicBuilder.name(NEW_CONVERSATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic newMessageTopic() {
        return TopicBuilder.name(NEW_MESSAGE_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic groupMessageRepliedTopic() {
        return TopicBuilder.name(GROUP_MESSAGE_REPLIED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic friendRequestAcceptedTopic() {
        return TopicBuilder.name(FRIEND_REQUEST_ACCEPTED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic newRegisteredUserTopic() {
        return TopicBuilder.name(NEW_REGISTERED_USER_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }



    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        addTypeMapping(props);

        ProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(props);

        return new KafkaTemplate<>(factory);
    }

    private void addTypeMapping(Map<String, Object> props) {
        Class[] producedEventClasses = {
                NewMessageEvent.class,
                NewConversationEvent.class,
                GroupMessageRepliedEvent.class
        };

        String typeMapping = "";
        for (Class eventClass : producedEventClasses) {
            String simpleName = eventClass.getSimpleName();
            String name = eventClass.getName();
            typeMapping += simpleName + ":" + name + ",";
        }
        // remove the last comma
        typeMapping = typeMapping.substring(0, typeMapping.length() - 1);

        // after the loop the typeMapping will be like this:
        // NewConversationEvent:com.imatalk.chat-service.events.NewConversationEvent, NewFriendRequestEvent:com.imatalk.chat-service.events.NewFriendRequestEvent,...

        props.put(JsonSerializer.TYPE_MAPPINGS, typeMapping);

    }
}
