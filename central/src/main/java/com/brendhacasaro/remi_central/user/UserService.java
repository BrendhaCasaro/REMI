package com.brendhacasaro.remi_central.user;

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
        UserEntity user = new UserEntity(
                request.username(),
                passwordEncoder.encode(request.password())
        );

        user = userRepository.save(user);
        return toResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse getUserById(Integer id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User id " + id + " not found"));

        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Integer id, UserRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User id " + id + " not found"));

        if (request.username() != null && !request.username().isBlank()) {
            user.setUsername(request.username());
        }

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public void deleteUser(Integer id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User id " + id + " not found"));

        userRepository.delete(user);
    }

    private UserResponse toResponse(UserEntity user) {
        return new UserResponse(user.getId(), user.getUsername());
    }
}
