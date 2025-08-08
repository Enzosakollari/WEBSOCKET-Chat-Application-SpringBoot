package com.enzo.websocket.User;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService service;


    @MessageMapping("/user.addUser")
    @SendTo("/user/topic")
    public User addUser(User user){
        service.saveUser(user);
        return user;
    }
    @MessageMapping("/user.disconnectUser")
    @SendTo("/user/topic")
    public User disconnet(@Payload User user){
        service.disconnectUser(user);
        return user;
    }
    @GetMapping("/user/connectedUsers")
    @SendTo("/topic/connectedUsers")
    public ResponseEntity<List<User>> getConnectedUsers(){
        return ResponseEntity.ok(service.findConnectedUsers());
    }


}
