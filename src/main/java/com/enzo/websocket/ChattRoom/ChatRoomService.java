package com.enzo.websocket.ChattRoom;

import com.enzo.websocket.User.User;
import com.enzo.websocket.User.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;  // Use UserService instead of repository

    public Optional<String> getChatRoomId(String senderNickname, String recipientNickname, boolean createIfNotFound) {
        // Find existing chat room
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByUsers(senderNickname, recipientNickname);
        if (existingRoom.isPresent()) {
            return existingRoom.map(ChatRoom::getChatId);
        }

        if (!createIfNotFound) {
            return Optional.empty();
        }

        // Get users from service
        User sender = userService.findByNickname(senderNickname)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User recipient = userService.findByNickname(recipientNickname)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        String chatId = generateChatId(senderNickname, recipientNickname);

        ChatRoom room = ChatRoom.builder()
                .id(UUID.randomUUID().toString())
                .user1(sender)
                .user2(recipient)
                .chatId(chatId)
                .build();

        chatRoomRepository.save(room);
        return Optional.of(chatId);
    }

    public Optional<ChatRoom> findByChatId(String chatId) {
        return chatRoomRepository.findByChatId(chatId);
    }

    public ChatRoom save(ChatRoom chatRoom) {
        return chatRoomRepository.save(chatRoom);
    }

    public List<ChatRoom> findByUserNickname(String nickname) {
        return chatRoomRepository.findByUserNickname(nickname);
    }

    private String generateChatId(String nickname1, String nickname2) {
        return nickname1.compareTo(nickname2) < 0
                ? nickname1 + "_" + nickname2
                : nickname2 + "_" + nickname1;
    }
}
