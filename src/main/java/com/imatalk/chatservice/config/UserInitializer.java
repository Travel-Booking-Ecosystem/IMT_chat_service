package com.imatalk.chatservice.config;

import com.imatalk.chatservice.dto.request.SendMessageRequest;
import com.imatalk.chatservice.entity.DirectConversation;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.repository.DirectConversationRepo;
import com.imatalk.chatservice.repository.MessageRepo;
import com.imatalk.chatservice.repository.UserRepo;
import com.imatalk.chatservice.service.ChatService;
import com.imatalk.chatservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.imatalk.chatservice.entity.DirectConversation.createDefaultSeenMessageTracker;

@Component
@RequiredArgsConstructor
public class UserInitializer implements CommandLineRunner {

    private final UserRepo userRepo;
    private final DirectConversationRepo directConversationRepo;
    private final MessageRepo messageRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        User minamino = User.builder()
                .email("minamino@test.com")
                .avatar("https://th.bing.com/th/id/OIP.ZpNOsfN4Tzl8UMtCe7j2kwHaE8?pid=ImgDet&w=192&h=128&c=7&dpr=1.3")
                .displayName("Takumi Minamino")
                .joinAt(LocalDateTime.now())
                .username("@minamino")
                .password(passwordEncoder.encode("1"))
                .directConversationInfoList(new ArrayList<>(0))
                .groupConversationInfoList(new ArrayList<>(0))
                .build();

        User billie = User.builder()
                .email("billie@test.com")
                .avatar("https://i.ytimg.com/vi/E9Ljxq_Sl-E/hqdefault.jpg")
                .displayName("Biilie Eilish")
                .joinAt(LocalDateTime.now())
                .username("@billie")
                .password(passwordEncoder.encode("1"))
                .directConversationInfoList(new ArrayList<>(0))
                .groupConversationInfoList(new ArrayList<>(0))
                .build();


        if (!userRepo.existsByEmail(minamino.getEmail()) && !userRepo.existsByEmail(billie.getEmail())) {
            // create users
            minamino = userRepo.save(minamino);
            billie = userRepo.save(billie);

            // create a conversation
            List<User> members = List.of(minamino, billie);
            DirectConversation directConversation = DirectConversation.builder()
                    .createdAt(LocalDateTime.now())
                    .members(members)
                    .seenMessageTracker(createDefaultSeenMessageTracker(members))
                    .messages(new ArrayList<>())
                    .build();

            directConversation = directConversationRepo.save(directConversation);

            // add conversation to each member's conversation list
            for (User member : members) {
                member.joinConversation(directConversation);
            }
            userRepo.saveAll(members);


            List<String> strings = List.of(
                    "Love when it comes without a warning",
                    "Cause waiting for it gets so boring",
                    "A lot can change in twenty seconds. A lot can happen in the dark.",
                    "Love when it makes you lose your bearings. Some information's not for sharing. Use different names at hotel check-ins",
                    "It's hard to stop it once it starts",
                    "My mommy likes to sing along with me. But she won't sing this song. If she reads all the lyrics. She'll pity the men I know. So you're a tough guy",
                    "Like it really rough guy, Just can't get enough guy, Chest always so puffed guy. I'm that bad type. Make your mama sad type. Make your girlfriend mad tight. Might seduce your dad type. I'm the bad guy. Duh"
            );

            // send a message 1
            Message message1 = createMessage(minamino, strings.get(0));
            message1.setConversationId(directConversation.getId());
            message1.setMessageNo(1);
            message1 = messageRepo.save(message1);

            // send a message 2
            Message message2 = createMessage(billie, strings.get(1));
            message2.setConversationId(directConversation.getId());
            message2.setMessageNo(2);
            message2.setRepliedMessageId(message1.getId());
            message2 = messageRepo.save(message2);

            // send a message 3
            Message message3 = createMessage(minamino, strings.get(2));
            message3.setConversationId(directConversation.getId());
            message3.setMessageNo(3);
            message3 = messageRepo.save(message3);

            // send a message 4
            Message message4 = createMessage(billie, strings.get(3));
            message4.setConversationId(directConversation.getId());
            message4.setMessageNo(4);
            message4.setRepliedMessageId(message3.getId());
            message4 = messageRepo.save(message4);

            // send a message 5
            Message message5 = createMessage(minamino, strings.get(4));
            message5.setConversationId(directConversation.getId());
            message5.setMessageNo(5);
            message5 = messageRepo.save(message5);

            // send a message 6
            Message message6 = createMessage(minamino, strings.get(5));
            message6.setConversationId(directConversation.getId());
            message6.setMessageNo(6);
            message6.setRepliedMessageId(message5.getId());
            message6 = messageRepo.save(message6);


            // send a message 7
            Message message7 = createMessage(billie, strings.get(6));
            message7.setConversationId(directConversation.getId());
            message7.setMessageNo(7);
            message7 = messageRepo.save(message7);




            // add messages to the conversation

            directConversation.addMessage(message1);
            directConversation.addMessage(message2);
            directConversation.addMessage(message3);
            directConversation.addMessage(message4);
            directConversation.addMessage(message5);
            directConversation.addMessage(message6);
            directConversation.addMessage(message7);
            directConversationRepo.save(directConversation);

            // move the conversation to the top of the list
            for (User member : members) {
                member.moveConversationToTop(directConversation);
            }
            userRepo.saveAll(members);
        }


    }

    private Message createMessage(User currentUser, String message) {
        return Message.builder()
                .senderId(currentUser.getId())
                .content(message)
                .repliedMessageId(null)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
