package com.enzo.websocket.Chat;

import com.enzo.websocket.ChattRoom.ChatRoom;
import com.enzo.websocket.ChattRoom.ChatRoomService;
import com.enzo.websocket.User.User;
import com.enzo.websocket.User.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import com.enzo.websocket.User.Status;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void processMessage(ChatMessage chatMessage) {
        try {
            // Ensure sender and receiver exist in database
            User sender = userService.findByNickname(chatMessage.getSender().getNickname())
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setNickname(chatMessage.getSender().getNickname());
                        newUser.setFullname(chatMessage.getSender().getNickname()); // Use nickname as fallback
                        newUser.setStatus(Status.ONLINE);
                        return userService.save(newUser);
                    });

            User receiver = userService.findByNickname(chatMessage.getReceiver().getNickname())
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setNickname(chatMessage.getReceiver().getNickname());
                        newUser.setFullname(chatMessage.getReceiver().getNickname()); // Use nickname as fallback
                        newUser.setStatus(Status.ONLINE);
                        return userService.save(newUser);
                    });

            // Set the actual User entities (not just the nickname strings)
            chatMessage.setSender(sender);
            chatMessage.setReceiver(receiver);

            // Save the message
            ChatMessage savedMessage = chatMessageService.save(chatMessage);

            // Create notification
            ChatNotification notification = ChatNotification.builder()
                    .messageId(savedMessage.getId())
                    .chatId(savedMessage.getChatId())
                    .senderNickname(savedMessage.getSender().getNickname())
                    .senderFullname(savedMessage.getSender().getFullname())
                    .receiverNickname(savedMessage.getReceiver().getNickname())
                    .content(savedMessage.getContent())
                    .timestamp(savedMessage.getTimestamp())
                    .status("DELIVERED")
                    .build();

            // Send to chat room topic
            String chatId = savedMessage.getChatId();
            messagingTemplate.convertAndSend("/topic/chat/" + chatId, notification);

            // Also send to receiver's private queue for immediate delivery
            messagingTemplate.convertAndSendToUser(
                    receiver.getNickname(),
                    "/queue/messages",
                    notification
            );

            // Send confirmation to sender
            messagingTemplate.convertAndSendToUser(
                    sender.getNickname(),
                    "/queue/messages",
                    notification
            );

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/addUser")
    @SendTo("/topic/users")
    public User addUser(User user) {
        return userService.saveUser(user);
    }

    @MessageMapping("/disconnectUser")
    @SendTo("/topic/users")
    public User disconnectUser(User user) {
        userService.disconnectUser(user.getNickname());
        return user;
    }

    @GetMapping("/messages/{senderNickname}/{receiverNickname}")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @PathVariable String senderNickname,
            @PathVariable String receiverNickname) {

        // 1. Find users
        User sender = userService.findByNickname(senderNickname)
                .orElseThrow(() -> new RuntimeException("Sender not found: " + senderNickname));
        User receiver = userService.findByNickname(receiverNickname)
                .orElseThrow(() -> new RuntimeException("Receiver not found: " + receiverNickname));

        // 2. Get or create chat room (create if not found)
        String chatId = chatRoomService.getChatRoomId(
                senderNickname,
                receiverNickname,
                true  // Create if not found
        ).orElseThrow(ChatRoomNotFoundException::new);

        // 3. Get messages
        return ResponseEntity.ok(chatMessageService.findByChatId(chatId));
    }

    @GetMapping("/chatrooms/{nickname}")
    public ResponseEntity<List<ChatRoom>> getChatRooms(@PathVariable String nickname) {
        // Validate user exists
        userService.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("User not found: " + nickname));

        // Get all chat rooms for the user
        return ResponseEntity.ok(chatRoomService.findByUserNickname(nickname));
    }
}
