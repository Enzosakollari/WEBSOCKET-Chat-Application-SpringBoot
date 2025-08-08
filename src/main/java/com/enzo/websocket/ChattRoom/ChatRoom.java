package com.enzo.websocket.ChattRoom;

import jakarta.persistence.*;
import lombok.*;
import com.enzo.websocket.User.User;
import java.util.List;
import com.enzo.websocket.Chat.ChatMessage;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {
    @Id
    private String id; // Changed from String to Long for standard ID

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;  // Must match "mappedBy" in ChatRoom

    @ManyToOne
    @JoinColumn(name = "user1_nickname",referencedColumnName = "nickname",nullable = false)
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_nickname", referencedColumnName = "nickname",nullable = false)
    private User user2;


    @Column(unique = true, nullable = false)

    private String chatId; // Unique identifier for the chat

//    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ChatMessage> messages;
}