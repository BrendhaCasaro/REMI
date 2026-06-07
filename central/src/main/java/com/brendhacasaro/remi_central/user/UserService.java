package com.brendhacasaro.remi_central.user;

import com.brendhacasaro.remi_central.user.dto.UserRequest;
import com.brendhacasaro.remi_central.user.dto.UserResponse;
import com.brendhacasaro.remi_central.user.model.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UserRequest request) {
        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.role()
        );

        user = userRepository.save(user);
        return toResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User id " + id + " not found"));

        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Integer id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User id " + id + " not found"));

        if (request.username() != null && !request.username().isBlank()) {
            user.setUsername(request.username());
        }

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        if (request.role() != null && request.role() != user.getRole()) {
            user.setRole(request.role());
        }

        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User id " + id + " not found"));

        userRepository.delete(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
