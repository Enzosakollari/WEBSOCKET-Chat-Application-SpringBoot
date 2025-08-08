package com.enzo.websocket.Chat;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String nickname) {
      super("User not found: " + nickname);
    }

}
