package com.enzo.websocket.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "user")
public class User {

    @Id
    @Column(name = "nickname", length = 50, unique = true, nullable = false)
    private String nickname;  // Primary key

    @Column(nullable = false)
    private String fullname;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ONLINE; // Default value
}