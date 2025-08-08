package com.enzo.websocket.Chat;

public class ChatRoomNotFoundException extends RuntimeException {
  public ChatRoomNotFoundException() {
    super("Chat room not found");
  }
}
