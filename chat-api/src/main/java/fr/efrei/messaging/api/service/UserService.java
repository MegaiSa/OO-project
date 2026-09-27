package fr.efrei.messaging.api.service;

import fr.efrei.messaging.api.entity.User;
import fr.efrei.messaging.api.exception.ConflictException;
import fr.efrei.messaging.api.exception.ResourceNotFoundException;
import fr.efrei.messaging.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User create(String username, String displayName) {
        String normalized = username.strip().toLowerCase();
        if (userRepository.existsByUsername(normalized)) {
            throw new ConflictException("Username '" + normalized + "' is already taken");
        }
        User user = userRepository.save(new User(normalized, displayName.strip()));
        log.info("User {} created (id={})", user.getUsername(), user.getId());
        return user;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional
    public User updateDisplayName(Long id, String displayName) {
        User user = findById(id);
        user.setDisplayName(displayName.strip());
        log.info("User {} renamed to '{}'", id, user.getDisplayName());
        return userRepository.save(user);
    }
}
