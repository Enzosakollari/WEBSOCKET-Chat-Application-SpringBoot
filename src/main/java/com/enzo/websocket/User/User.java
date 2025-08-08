package com.enzo.websocket.User;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name="User")
public class User {
    @Id
    @Column(name = "nickname", length = 50, unique = true, nullable = false, updatable = false)
    private String nickname;

    @Column(nullable=false)
    private String fullname;

    @Column(length=20)
    @Enumerated(EnumType.STRING)
    private Status status;



}
