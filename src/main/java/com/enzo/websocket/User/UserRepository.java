package com.enzo.websocket.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {


public static Optional<User> findByNickname(String nickname1){

    return null;
};
}