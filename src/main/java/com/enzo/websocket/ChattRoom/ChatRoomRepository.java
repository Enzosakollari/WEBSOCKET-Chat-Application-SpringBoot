package com.enzo.websocket.ChattRoom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,String> {


    @Query("SELECT c FROM ChatRoom c WHERE " +
            "(c.user1.nickname = :nickname1 AND c.user2.nickname = :nickname2) OR " +
            "(c.user1.nickname = :nickname2 AND c.user2.nickname = :nickname1)")
    Optional<ChatRoom> findByUsers(
            @Param("nickname1") String nickname1,
            @Param("nickname2") String nickname2
    );



}


