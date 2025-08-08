package com.enzo.websocket.ChattRoom;


import com.enzo.websocket.User.User;
import com.enzo.websocket.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor


public class ChatRoomService {

    private final ChatRoomRepository repository;
    private final UserRepository userRepository;



    public Optional<String> getChatRoomId(String nickname1, String nickname2, boolean createIfNotFound) {
        // First try to find existing chat
        Optional<ChatRoom> existingRoom = repository.findByUsers(nickname1, nickname2);

        if (existingRoom.isPresent()) {
            return existingRoom.map(ChatRoom::getChatId);
        }

        if (!createIfNotFound) {
            return Optional.empty();  // Proper empty Optional instead of null
        }

        // Create new chat room
        User user1 = UserRepository.findByNickname(nickname1)
                .orElseThrow(() -> new RuntimeException("User not found: " + nickname1));
        User user2 = UserRepository.findByNickname(nickname2)
                .orElseThrow(() -> new RuntimeException("User not found: " + nickname2));

        String chatId = nickname1.compareTo(nickname2) < 0
                ? nickname1 + "_" + nickname2
                : nickname2 + "_" + nickname1;

        ChatRoom room = ChatRoom.builder()
                .user1(user1)
                .user2(user2)
                .chatId(chatId)
                .build();

        repository.save(room);
        return Optional.of(chatId);  // Explicit Optional wrapper
    }
}

