package com.enzo.websocket.Chat;

import com.enzo.websocket.Chat.ChatMessage;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByChatId(String chatId);
}