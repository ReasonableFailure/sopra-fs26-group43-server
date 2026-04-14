package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {
    protected final UserRepository userRepository;

    public PlayerMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findUserByID(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}
