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


            // send a message
            Message message = createMessage(minamino);
            message.setConversationId(directConversation.getId());
            message.setMessageNo(1);
            message = messageRepo.save(message);

            directConversation.addMessage(message);
            directConversationRepo.save(directConversation);

            // move the conversation to the top of the list
            for (User member : members) {
                member.moveConversationToTop(directConversation);
            }
            userRepo.saveAll(members);
        }


    }

    private Message createMessage(User currentUser) {
        return Message.builder()
                .senderId(currentUser.getId())
                .content("Hello Billie")
                .repliedMessageId(null)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
