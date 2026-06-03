package com.brendhacasaro.remi_central.user;

import com.brendhacasaro.remi_central.user.dto.UserRequest;
import com.brendhacasaro.remi_central.user.dto.UserResponse;
import com.brendhacasaro.remi_central.user.model.Role;
import com.brendhacasaro.remi_central.user.model.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_shouldPersistAndReturnResponse() {
        when(passwordEncoder.encode("rawPass")).thenReturn("encodedPass");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(u, 1);
            return u;
        });

        UserResponse response = userService.createUser(
                new UserRequest("newuser", "rawPass", Role.ADMIN));

        assertNotNull(response);
        assertEquals("newuser", response.username());
        assertEquals(1, response.id());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("newuser", captor.getValue().getUsername());
        assertEquals("encodedPass", captor.getValue().getPassword());
        assertEquals(Role.ADMIN, captor.getValue().getRole());
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        User user1 = new User("user1", "pass1", Role.ADMIN);
        User user2 = new User("user2", "pass2", Role.USER);
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponse> users = userService.getAllUsers();

        assertEquals(2, users.size());
        assertEquals("user1", users.get(0).username());
        assertEquals("user2", users.get(1).username());
    }

    @Test
    void getUserById_shouldReturnUser() {
        User user = new User("existing", "pass", Role.USER);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1);

        assertEquals("existing", response.username());
    }

    @Test
    void getUserById_shouldThrowWhenNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(99));
    }

    @Test
    void updateUser_shouldUpdateFields() {
        User existing = new User("oldname", "oldPass", Role.USER);
        when(userRepository.findById(1)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");
        when(userRepository.save(any())).thenReturn(existing);

        UserResponse response = userService.updateUser(1,
                new UserRequest("newname", "newPass", Role.ADMIN));

        assertEquals("newname", response.username());
        assertEquals("newname", existing.getUsername());
        assertEquals("encodedNewPass", existing.getPassword());
    }

    @Test
    void updateUser_shouldIgnoreNullUsername() {
        User existing = new User("keepname", "oldPass", Role.USER);
        when(userRepository.findById(1)).thenReturn(Optional.of(existing));
        when(userRepository.save(any())).thenReturn(existing);

        userService.updateUser(1, new UserRequest(null, null, Role.ADMIN));

        assertEquals("keepname", existing.getUsername());
        assertEquals("oldPass", existing.getPassword());
    }

    @Test
    void updateUser_shouldThrowWhenNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.updateUser(99, new UserRequest("x", "y", Role.USER)));
    }

    @Test
    void deleteUser_shouldRemoveUser() {
        User user = new User("todelete", "pass", Role.USER);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        userService.deleteUser(1);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_shouldThrowWhenNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(1));
    }
}
