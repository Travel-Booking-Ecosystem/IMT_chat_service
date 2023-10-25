package com.imatalk.chatservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ToString(exclude = {"conversation"})
public class MessageSeen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // the reason why i use Long as id for this class, not String like other class
    // , is because the instance of this class will only be automatically created by the application when a user see a message
    // the id of instances of this class should not be set before saving to database
    // because the instance of this class will be saved to database along with the Conversation entity via cascade
    // and with the @GeneratedValue annotation, it allows Spring to automatically generate the id for this class
    private Long id;

    @ManyToOne
    @JsonIgnore
    private Conversation conversation;
    private String userId;
    private long lastSeenMessageNo;
}
