package com.enzo.websocket.Chat;

import com.enzo.websocket.Chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

List<ChatMessage> findByChatId(String chatId);

}
