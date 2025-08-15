package com.enzo.websocket.User;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/user/connectedUsers")
    public ResponseEntity<List<User>> getConnectedUsers() {
        return ResponseEntity.ok(userService.getConnectedUsers());
    }

    @MessageMapping("/user.addUser")
    @SendTo("/topic/users")
    public User addUser(@Payload Map<String, String> payload) {
        String nickname = payload.get("nickname");
        String fullname = payload.get("fullname");

        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("Nickname is required");
        }
        if (fullname == null || fullname.trim().isEmpty()) {
            throw new IllegalArgumentException("Fullname is required");
        }

        User user = new User();
        user.setNickname(nickname);
        user.setFullname(fullname);

        return userService.saveUser(user);
    }

    @MessageMapping("/user.disconnectUser")
    @SendTo("/topic/users")
    public User disconnect(@Payload Map<String, String> payload) {
        String nickname = payload.get("nickname");
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("Nickname is required for disconnection");
        }

        userService.disconnectUser(nickname);
        User user = new User();
        user.setNickname(nickname);
        return user;
    }
}