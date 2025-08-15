package com.enzo.websocket.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;

    public List<User> getConnectedUsers() {
        return repository.findAll();
    }

    // Return Optional<User> for safer handling
    public Optional<User> findByNickname(String nickname) {
        return repository.findByNickname(nickname);
    }

    // Save method for creating/updating users
    @Transactional
    public User save(User user) {
        // Only set status to ONLINE if it's not already set
        if (user.getStatus() == null) {
            user.setStatus(Status.ONLINE);
        }
        return repository.save(user);
    }

    // Alias for save method to maintain backward compatibility
    @Transactional
    public User saveUser(User user) {
        user.setStatus(Status.ONLINE); // Always set to ONLINE
        return repository.save(user);
    }

    @Transactional
    public void disconnectUser(String nickname) {
        repository.findById(nickname).ifPresent(user -> {
            user.setStatus(Status.OFFLINE);
            repository.save(user);
        });
    }
}
