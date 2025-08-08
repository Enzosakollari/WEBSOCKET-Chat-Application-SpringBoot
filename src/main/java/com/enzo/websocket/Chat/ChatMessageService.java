package com.enzo.websocket.Chat;
import com.enzo.websocket.ChattRoom.ChatRoomService;
import com.enzo.websocket.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository repository;
    private final ChatRoomService chatRoomService;


    public List<ChatMessage> findByChatId(String chatId) {
        return repository.findByChatId(chatId);
    }
    public ChatMessage save(ChatMessage message) {
        // Generate chatId (format: "sender_receiver" sorted)
        String chatId = Stream.of(
                        message.getSender().getNickname(),
                        message.getReceiver().getNickname()
                )
                .sorted()
                .collect(Collectors.joining("_"));

        message.setChatId(chatId);  // Just set the string!
        repository.save(message);
        return message;
    }
    public List<ChatMessage> findChatMessage(User sender, User receiver) {
        var chatId = chatRoomService.getChatRoomId(sender.getNickname(), receiver.getNickname(), false);
        return chatId.map(repository::findByChatId)
                .orElse(new ArrayList<>());
    }

}
