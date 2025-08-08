package com.enzo.websocket.Chat;

import com.enzo.websocket.ChattRoom.ChatRoom;
import jakarta.persistence.*;
import lombok.*;
import com.enzo.websocket.User.User;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    private String id;

    private String chatId;

    @ManyToOne
    @JoinColumn(name = "sender_nickname", referencedColumnName = "nickname")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_nickname", referencedColumnName = "nickname")
    private User receiver;


    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();


}