package com.enzo.websocket.User;


import com.enzo.websocket.Chat.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class UserService {

    private final UserRepository repository;


public void saveUser(User user){
    user.setStatus(Status.ONLINE);
    repository.save(user);
}
public void disconnectUser(User user){

    var storedUser = repository.findById(user.getNickname());
    if(storedUser.isPresent()){
        storedUser.get().setStatus(Status.OFFLINE);
        repository.save(storedUser.get());
    }

}

    public User findByNickname(String nickname) {
        return UserRepository.findByNickname(nickname)
                .orElseThrow(() -> new UserNotFoundException(nickname));
    }
public List<User> findConnectedUsers(){return null;}

}
