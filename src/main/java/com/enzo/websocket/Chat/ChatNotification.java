package com.enzo.websocket.Chat;
import com.enzo.websocket.User.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatNotification {
    private String messageId;      // Matches ChatMessage.id
    private String chatId;        // Matches ChatMessage.chatId

    // Sender/receiver info (extracted from relationships)
    private String senderNickname;
    private String senderFullname;
    private String receiverNickname;

    private String content;
    private LocalDateTime timestamp;
    private String status;        // e.g., "DELIVERED", "READ"

    // Conversion method that respects your JPA relationships
    public static ChatNotification fromEntity(ChatMessage message) {
        return ChatNotification.builder()
                .messageId(message.getId())
                .chatId(message.getChatId())
                .senderNickname(message.getSender().getNickname())
                .senderFullname(message.getSender().getFullname())
                .receiverNickname(message.getReceiver().getNickname())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .status("DELIVERED") // Default status
                .build();
    }
}