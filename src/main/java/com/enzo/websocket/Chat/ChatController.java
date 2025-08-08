package com.enzo.websocket.Chat;


import com.enzo.websocket.ChattRoom.ChatRoomService;
import com.enzo.websocket.User.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;



    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage message) {
        // 1. Save message (relationships preserved via your JPA annotations)
        ChatMessage savedMsg = chatMessageService.save(message);

        // 2. Convert to notification DTO
        ChatNotification notification = ChatNotification.fromEntity(savedMsg);

        // 3. Send to recipient's private queue
        messagingTemplate.convertAndSendToUser(
                savedMsg.getReceiver().getNickname(), // From JPA relationship
                "/queue/messages",
                notification
        );

        // 4. Optional: Send to chat room topic
        messagingTemplate.convertAndSend(
                "/topic/chat/" + savedMsg.getChatId(),
                notification
        );
    }

    @GetMapping("/messages/{senderNickname}/{receiverNickname}")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @PathVariable String senderNickname,
            @PathVariable String receiverNickname) {

        // 1. Find users
        User sender = userService.findByNickname(senderNickname);
        User receiver = userService.findByNickname(receiverNickname);

        // 2. Get chat ID
        String chatId = chatRoomService.getChatRoomId(
                senderNickname,
                receiverNickname,
                false
        ).orElseThrow(ChatRoomNotFoundException::new);

        // 3. Get messages
        return ResponseEntity.ok(chatMessageService.findByChatId(chatId));
    }
}