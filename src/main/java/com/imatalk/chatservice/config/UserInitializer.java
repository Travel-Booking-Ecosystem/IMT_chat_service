package com.imatalk.chatservice.config;

import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.FriendRequest;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.repository.ConversationRepo;
import com.imatalk.chatservice.repository.FriendRequestRepo;
import com.imatalk.chatservice.repository.MessageRepo;
import com.imatalk.chatservice.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.imatalk.chatservice.entity.Conversation.createDefaultSeenMessageTracker;

@Component
@RequiredArgsConstructor
public class UserInitializer implements CommandLineRunner {

    private final UserRepo userRepo;
    private final ConversationRepo directConversationRepo;
    private final MessageRepo messageRepo;
    private final PasswordEncoder passwordEncoder;
    private final FriendRequestRepo friendRequestRepo;
    @Override
    @Transactional
    public void run(String... args) throws Exception {

        List<User> users = new ArrayList<User>();
        User minamino = createUser(
                "minamino@test.com",
                "https://th.bing.com/th/id/OIP.ZpNOsfN4Tzl8UMtCe7j2kwHaE8?pid=ImgDet&w=192&h=128&c=7&dpr=1.3",
                "Takumi Minamino",
                "@minamino"
        );
        users.add(minamino);

        User billie = createUser(
                "billie@test.com",
                "https://i.ytimg.com/vi/E9Ljxq_Sl-E/hqdefault.jpg",
                "Billie Eilish",
                "@billie"
        );
        users.add(billie);

        User tienAnh = createUser(
                "tienanh@test.com",
                "https://images.unsplash.com/photo-1577473403731-a36ec9087f44?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=60",
                "Tiến Ánh",
                "@tienanh"
        );
        users.add(tienAnh);

        User strange = createUser(
                "strange@test.com",
                "https://th.bing.com/th/id/OIP.VlLr8K7rrnHJHidVONxBlAHaI_?w=154&h=186&c=7&r=0&o=5&dpr=1.3&pid=1.7",
                "My Strange Addiction",
                "@strange"
        );
        users.add(strange);

        User nam = createUser(
                "nam@test.com",
                "https://images.unsplash.com/photo-1694747994681-67791c336f2c?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=60",
                "Nam Nguyễn",
                "@nam"
        );
        users.add(nam);

        User lam = createUser(
                "lam@test.com",
                "https://images.unsplash.com/photo-1581263518256-ba4a28ed5517?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTZ8fGNvbmFufGVufDB8fDB8fHww&auto=format&fit=crop&w=600&q=60",
                "Trần Như Lâm",
                "@lam"
        );
        users.add(lam);

        User hai = createUser(
                "hai@gmail.com",
                "https://images.unsplash.com/photo-1582128277384-cdda7717bd12?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Nnx8aGVsbWV0fGVufDB8fDB8fHww&auto=format&fit=crop&w=600&q=60",
                "Hải Nguyễn",
                "@hai"
        );
        users.add(hai);


        if (!userRepo.existsByEmail(minamino.getEmail())
                && !userRepo.existsByEmail(billie.getEmail())) {
            // create users
            userRepo.saveAll(users);

            List<String> minanino_billie = List.of(
                    "Love when it comes without a warning",
                    "Cause waiting for it gets so boring",
                    "A lot can change in twenty seconds. A lot can happen in the dark.",
                    "Love when it makes you lose your bearings. Some information's not for sharing. Use different names at hotel check-ins",
                    "It's hard to stop it once it starts",
                    "My mommy likes to sing along with me. But she won't sing this song. If she reads all the lyrics. She'll pity the men I know. So you're a tough guy",
                    "I'm that bad type. Make your mama sad type. Make your girlfriend mad tight. Might seduce your dad type. I'm the bad guy. Duh",
                    "I'm the bad guy. Duh",
                    "I'm only good at being bad, bad",
                    "I like when you get mad. I guess I'm pretty glad that you're alone",
                    "You said she's scared of me? I mean, I don't see what she sees. But maybe it's 'cause I'm wearing your cologne",
                    "I'm a bad guy. I'm a bad guy. Bad guy, bad guy",
                    "I'm a bad guy. Bad guy, bad guy",
                    "I'm a bad guy",
                    "I like it when you take control. Even if you know that you don't own me. I'll let you play the role. I'll be your animal",
                    "Like it really rough guy, Just can't get enough guy, Chest always so puffed guy. I'm that bad type. Make your mama sad type. Make your girlfriend mad tight. Might seduce your dad type. I'm the bad guy. Duh"
            );

            List<String> minamino_strange = List.of(
                    "I love Billie Eilish's music. Her lyrics are so relatable.",
                    "Me too. I especially like the way she sings about love and relationships.",
                    "Yeah, she's not afraid to be honest about her experiences, even if they're messy or complicated.",
                    "Like in \"Bad Guy,\" she sings about how she's the bad type, but she's also the one who's in control.",
                    "And in \"ilomilo,\" she sings about how love can be both beautiful and destructive.",
                    "I think that's what makes her music so compelling. She's not afraid to explore the dark side of love, but she also shows that it's possible to find beauty in the messiness.",
                    "Her music is a reminder that love is complex and that there's no one right way to experience it.",
                    "So, what's your favorite Billie Eilish song?",
                    "That's a tough question. I love them all, but if I had to choose one, it would probably be \"Ocean Eyes.\"",
                    "That's a good choice. It's such a beautiful and haunting song.",
                    "Yeah, and the lyrics are so poetic. I love the way she describes the ocean as a metaphor for love.",
                    "It's a song that I can listen to over and over again and always find something new to appreciate.",
                    "I think that's what makes Billie Eilish such a great artist. Her music is timeless and universal. It speaks to something deep inside of us, even if we don't always understand it.",
                    "She's a truly gifted songwriter.",
                    "I'm so glad that we have Billie Eilish in the world. Her music makes life a little bit more beautiful."
            );


            List<String> minamino_tienAnh = List.of(
                    "I appreciate Billie Eilish's artistry. Her songs have a depth that's rare in popular music.",
                    "Absolutely. Her lyrics often tackle complex emotions and situations.",
                    "And the way she presents those themes with her music and visuals is truly unique.",
                    "I think she's inspiring a whole new generation of musicians and artists.",
                    "Her impact on the industry is undeniable. She's paving the way for a more introspective and genuine approach to music."
            );

            List<String> billie_strange = List.of(
                    "Billie Eilish's music videos are like mini movies. I love the storytelling in them.",
                    "Definitely. Each video adds a new layer of meaning to her songs.",
                    "I love Billie Eilish's music. Her lyrics are so relatable and deep.",
                    "Absolutely, her songs resonate with so many people on a personal level.",
                    "Her ability to capture complex emotions in her music is truly remarkable.",
                    "I think her authenticity is what sets her apart from other artists.",
                    "Definitely, she's not afraid to be vulnerable in her music and express her true feelings.",
                    "And her music videos are like works of art, they add another layer to her storytelling.",
                    "Yes, each video enhances the meaning of the song and creates a unique visual experience.",
                    "I appreciate how she addresses mental health and struggles in her songs.",
                    "It's powerful how she can turn pain into something beautiful and inspiring.",
                    "Her impact on the music industry and her fans is undeniable.",
                    "Absolutely, she's inspiring a whole new generation of musicians and listeners.",
                    "I love discussing the themes and metaphors in her songs, there's always so much to unpack.",
                    "Agreed, her lyrics often have multiple layers of meaning, making them open to interpretation.",
                    "Her music creates a sense of connection, like she understands what we're going through.",
                    "Yes, that emotional connection is what makes her music timeless and relatable.",
                    "And the visuals are always so thought-provoking. They stay with you long after you've watched them.",
                    "I think that's what sets her apart. She's not just a singer; she's a storyteller."
            );

            List<String> billie_tienAnh = List.of(
                    "Have you ever tried analyzing Billie Eilish's lyrics? They are so rich in metaphors and symbolism.",
                    "Yes, her lyrics are like poetry. There are multiple layers of meaning in each line.",
                    "I love Billie Eilish's music. Her lyrics are so relatable and deep.",
                    "Absolutely, her songs resonate with so many people on a personal level.",
                    "Her ability to capture complex emotions in her music is truly remarkable.",
                    "I think her authenticity is what sets her apart from other artists.",
                    "Definitely, she's not afraid to be vulnerable in her music and express her true feelings.",
                    "And her music videos are like works of art, they add another layer to her storytelling.",
                    "Yes, each video enhances the meaning of the song and creates a unique visual experience.",
                    "I appreciate how she addresses mental health and struggles in her songs.",
                    "It's powerful how she can turn pain into something beautiful and inspiring.",
                    "Her impact on the music industry and her fans is undeniable.",
                    "Absolutely, she's inspiring a whole new generation of musicians and listeners.",
                    "I love discussing the themes and metaphors in her songs, there's always so much to unpack.",
                    "Agreed, her lyrics often have multiple layers of meaning, making them open to interpretation.",
                    "Her music creates a sense of connection, like she understands what we're going through.",
                    "Yes, that emotional connection is what makes her music timeless and relatable.",
                    "It's amazing how she can convey such complex emotions with just a few words.",
                    "I think that's the mark of a true artist. She connects with listeners on a deep, emotional level."
            );

            List<String> strange_tienAnh = List.of(
                    "I've always been fascinated by the cosmos. The idea of endless galaxies and stars is incredible.",
                    "Space exploration has led to so many technological advancements. It's amazing what we've achieved as a species.",
                    "Did you know that a day on Venus is longer than its year? It rotates on its axis incredibly slowly.",
                    "Black holes are one of the most mysterious phenomena in the universe. The way they bend spacetime is mind-boggling.",
                    "I recently read about exoplanets. The possibility of other planets supporting life is both exciting and daunting.",
                    "The Hubble Space Telescope has given us breathtaking images of distant galaxies. It's like peering into the unknown.",
                    "Space agencies like NASA and SpaceX are making plans for Mars colonization. The idea of humans living on another planet is becoming a reality.",
                    "I find the concept of wormholes fascinating. The idea of traveling through space and time is like something out of science fiction.",
                    "Astrobiology, the study of extraterrestrial life, is a field that's gaining more attention. Imagine discovering life beyond Earth!",
                    "Studying the cosmic microwave background radiation has provided valuable insights into the early universe. It's like looking back in time.",
                    "The search for dark matter and dark energy is ongoing. We still have so much to learn about the fundamental nature of the universe.",
                    "Space is a reminder of how small we are in the grand scheme of things. It humbles me to think about the vastness of the cosmos."
            );


            List<String> minamino_nam = List.of(
                    "I just finished reading \"To Kill a Mockingbird\". It's a classic that really makes you think about justice and morality.",
                    "Oh, I love that book! Have you read \"1984\"? It's a dystopian novel that feels eerily relevant even today.",
                    "Yes, \"1984\" is a masterpiece. Speaking of dystopia, I recently read \"Brave New World\" too. It's another thought-provoking book.",
                    "\"Brave New World\" is incredible. It's amazing how authors can imagine such complex societies. Have you ever tried reading some non-fiction, like \"Sapiens\"?",
                    "Absolutely, I'm a big fan of non-fiction too. \"Sapiens\" gives you a whole new perspective on human history. Speaking of history, I also enjoyed \"The Immortal Life of Henrietta Lacks\".",
                    "\"The Immortal Life of Henrietta Lacks\" is so moving. It shows the impact one person can have on the world, even without knowing it. Have you read any good fantasy books recently?",
                    "Yes, I read \"The Name of the Wind\". It's the first book in a fantasy series and it completely captivated me. The world-building is phenomenal.",
                    "\"The Name of the Wind\" is on my to-read list! Fantasy worlds are so immersive. Have you ever tried science fiction? \"Dune\" is a classic.",
                    "I haven't read \"Dune\" yet, but I've heard great things. Science fiction has this unique ability to explore complex scientific and philosophical concepts. It's fascinating.",
                    "Definitely! Books have this magical way of transporting us to different worlds, whether they are real or imagined. It's one of the things I love most about reading."
            );

//            List<String> minamino_lam = List.of(
//                    "旅行は新しい文化を学ぶ絶好の機会です。どの国に行くつもりですか？",
//                    "日本の観光地は素晴らしいです。京都の寺院や東京の高層ビルなど、見どころがたくさんあります。",
//                    "日本の伝統的な食べ物は美味しいです。寿司やラーメンなど、現地の料理を楽しんでください。",
//                    "温泉は日本の文化の一部です。温泉地でのんびりとくつろぐのも良いですね。",
//                    "日本の交通システムは非常に効率的です。新幹線で移動すると、短時間で多くの場所に行けます。",
//                    "日本の祭りやイベントは非常にカラフルで楽しいです。桜の季節には花見がおすすめです。",
//                    "日本の歴史や文化に興味を持ってください。古代の寺院や城、伝統的な舞や音楽などが体験できます。",
//                    "親切な地元の人々と交流することで、旅行体験はより充実したものになります。",
//                    "旅行中には、現地の言葉や習慣を尊重することが大切です。地元の人々とのコミュニケーションを楽しんでください。",
//                    "旅行は人生の中で最も豊かな経験のひとつです。新しい場所や人々との出会いを大切にしてください。"
//            );

//            List<String> minamino_hai = List.of(
//                    "Các chiếc xe đua F1 ngày nay đều được thiết kế để đạt tốc độ cực cao trên đường đua.",
//                    "Mỗi chiếc xe F1 đều là kết quả của nhiều năm nghiên cứu và phát triển công nghệ.",
//                    "Hệ thống động cơ trên các chiếc xe F1 thực sự là một ki marvel kỹ thuật, đem lại công suất đáng kinh ngạc.",
//                    "Các đội đua F1 không chỉ cạnh tranh về tốc độ, mà còn về chiến lược và kỹ thuật lái xe.",
//                    "Ở tốc độ cao, các hệ thống an toàn trên xe đua F1 trở thành yếu tố quan trọng nhất để bảo vệ tay lái.",
//                    "Các tay đua F1 phải có kỹ năng tuyệt vời để điều khiển xe ở tốc độ cực nhanh và giữ cho nó ổn định trên đường đua.",
//                    "Cấu trúc nhẹ và sức mạnh động cơ của các chiếc F1 giúp chúng tăng tốc độ với tốc độ kinh ngạc.",
//                    "Đội kỹ thuật của mỗi đội đua F1 thường xuyên cải tiến và tối ưu hóa các thành phần của xe để đạt hiệu suất tốt nhất.",
//                    "Dù điều kiện thời tiết có thể thay đổi, các tay đua F1 vẫn phải thích ứng và lái xe ở tốc độ tối đa.",
//                    "Mỗi mùa giải F1 đều mang đến những trận đấu căng thẳng và hấp dẫn, là sự kết hợp của kỹ năng, chiến lược và may mắn.",
//                    "Đua xe F1 không chỉ là một môn thể thao, mà còn là một nghệ thuật kết hợp giữa tốc độ và chiến thuật.",
//                    "Mỗi đội đua F1 đều có các chiến lược đua riêng, bao gồm lịch trình dừng hạng, chiến thuật pit stop và quản lý lốp.",
//                    "Đua xe F1 đòi hỏi sự tập trung tuyệt đối và phản ứng nhanh với các biến động trên đường đua.",
//                    "Cảm giác khi lái một chiếc F1 ở tốc độ cao không giống với bất kỳ loại xe nào khác, đó là trải nghiệm độc đáo và gây nhiều kích thích.",
//                    "Các tay đua F1 phải không chỉ giỏi lái xe, mà còn phải hiểu rõ về kỹ thuật, đặc điểm của xe và cảm nhận được đường đua.",
//                    "Đua xe F1 không chỉ là cuộc thi tốc độ, mà còn là thử thách về sức bền và tinh thần. Mỗi cuộc đua kéo dài hàng chục vòng đua với tốc độ cao.",
//                    "Mỗi chiếc xe F1 được thiết kế và điều chỉnh đặc biệt để đạt hiệu suất tối đa trên từng loại đường đua.",
//                    "Đua đội và chiến lược đua là yếu tố quan trọng, các tay đua thường phối hợp với đồng đội để đạt được kết quả tốt nhất.",
//                    "Thời gian pit stop là quyết định quan trọng, một pit stop nhanh và chính xác có thể thay đổi cả kịch bản của cuộc đua.",
//                    "F1 không chỉ thu hút người hâm mộ với tốc độ, mà còn với những câu chuyện và chiến tích đằng sau mỗi chiếc xe và tay đua.",
//                    "Cảm giác chiến thắng ở F1 không chỉ là niềm vinh dự cá nhân, mà còn là thành quả của cả đội ngũ lớn và sự hỗ trợ từ các nhà tài trợ."
//            );

            createDirectConversationBetween2Users(minamino, billie, minanino_billie);
            createDirectConversationBetween2Users(minamino, strange, minamino_strange);
            createDirectConversationBetween2Users(minamino, tienAnh, minamino_tienAnh);
            createDirectConversationBetween2Users(minamino, nam, minamino_nam);
//            createDirectConversationBetween2Users(minamino, lam, minamino_lam);
//            createDirectConversationBetween2Users(minamino, hai, minamino_hai);


            createDirectConversationBetween2Users(billie, strange, billie_strange);
            createDirectConversationBetween2Users(billie, tienAnh, billie_tienAnh);
            createDirectConversationBetween2Users(strange, tienAnh, strange_tienAnh);


            addFriend(minamino, billie);
            addFriend(minamino, tienAnh);
            addFriend(minamino, strange);
            addFriend(minamino, nam);
            saveFriendRequest(lam, minamino);
            saveFriendRequest(hai, minamino);

        }


    }

    private User createUser(String email, String avatar, String displayName, String username) {
        return User.builder()
                .email(email)
                .avatar(avatar)
                .displayName(displayName)
                .joinAt(LocalDateTime.now())
                .username(username)
                .password(passwordEncoder.encode("1"))
                .conversations(new ArrayList<>(0))
                .friends(new ArrayList<>())
                .receivedFriendRequests(new ArrayList<>())
                .build();
    }

    public void createDirectConversationBetween2Users(User user1, User user2, List<String> strings) {
        // create a conversation
        List<User> members = List.of(user1, user2);
        Conversation directConversation = Conversation.builder()
                .createdAt(LocalDateTime.now())
                .members(members)
                .seenMessageTracker(createDefaultSeenMessageTracker(members))
                .messages(new ArrayList<>())
                .build();

        directConversation = directConversationRepo.save(directConversation);
        System.out.println("Save the conversation between " + user1.getDisplayName() + " and " + user2.getDisplayName());
        // add conversation to each member's conversation list
        for (User member : members) {
            member.joinConversation(directConversation);
        }
        userRepo.saveAll(members);


        List<Message> messages = new ArrayList<Message>();
        for (int i = 0; i < strings.size(); i++) {
            User sender = getRandomUserInMemberList(members);
            Message message = createMessage(sender, strings.get(i));
            message.setConversationId(directConversation.getId());
            message.setMessageNo(i + 1);
            setRandomRepliedMessageInConversation(message, messages);
            messageRepo.save(message);

            messages.add(message);
            directConversation.addMessage(message);
        }

        directConversationRepo.save(directConversation);

        // move the conversation to the top of the list
        user1.setCurrentConversationId(directConversation.getId());
        user2.setCurrentConversationId(directConversation.getId());
        userRepo.saveAll(members);
    }

    private User getRandomUserInMemberList(List<User> members) {
        int randomIndex = (int) (Math.random() * members.size());
        return members.get(randomIndex);
    }

    private void setRandomRepliedMessageInConversation(Message message, List<Message> messages) {
        boolean replyToOtherMessage = Math.random() < 0.5;

        if (replyToOtherMessage && messages.size() > 0) {
            int randomIndex = (int) (Math.random() * messages.size());
            Message repliedMessage = messages.get(randomIndex);
            message.setRepliedMessageId(repliedMessage.getId());
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


    private void addFriend(User user1, User user2) {
        System.out.println("Try to add " + user2.getDisplayName() + " to " + user1.getDisplayName() + "'s friend list");
        List<User> user1FriendList = user1.getFriends();
        user1FriendList.add(user2);

        List<User> user2FriendList = user2.getFriends();
        user2FriendList.add(user1);

        userRepo.saveAll(List.of(user1, user2));
//        userRepo.save(user2);
        System.out.println("Add " + user2.getDisplayName() + " to " + user1.getDisplayName() + "'s friend list");
    }

    private void saveFriendRequest(User requestSender, User requestReceiver) {
        System.out.println("Try to save a friend request from " + requestSender.getDisplayName() + " to " + requestReceiver.getDisplayName());
        FriendRequest request = FriendRequest.builder()
                .createdAt(LocalDateTime.now())
                .sender(requestSender)
                .receiver(requestReceiver)
                .isAccepted(false)
                .build();

        friendRequestRepo.save(request);

        requestReceiver.getReceivedFriendRequests().add(request);
        userRepo.save(requestReceiver);

        System.out.println("Save a friend request from " + requestSender.getDisplayName() + " to " + requestReceiver.getDisplayName());
    }
}
