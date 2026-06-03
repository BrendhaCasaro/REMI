package com.brendhacasaro.remi_central.user;

import com.brendhacasaro.remi_central.config.TestcontainersConfig;
import com.brendhacasaro.remi_central.user.model.Role;
import com.brendhacasaro.remi_central.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindByUsername() {
        User user = new User("johndoe", "securePass", Role.ADMIN);
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("johndoe");

        assertTrue(found.isPresent());
        assertEquals("johndoe", found.get().getUsername());
        assertEquals("securePass", found.get().getPassword());
        assertEquals(Role.ADMIN, found.get().getRole());
    }

    @Test
    void findByUsername_shouldReturnEmptyWhenNotFound() {
        Optional<User> found = userRepository.findByUsername("nonexistent");

        assertTrue(found.isEmpty());
    }

    @Test
    void deleteUser() {
        User user = new User("todelete", "pass", Role.USER);
        user = userRepository.save(user);

        userRepository.delete(user);

        assertTrue(userRepository.findById(user.getId()).isEmpty());
    }
}
