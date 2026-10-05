package com.chocolateshop.service;

import com.chocolateshop.dto.UserDto;
import com.chocolateshop.entity.AppUser;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<AppUser> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AppUser getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public AppUser getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
    }

    @Transactional
    public AppUser createUser(UserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + dto.getUsername());
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required for new user");
        }

        AppUser user = AppUser.builder()
                .username(dto.getUsername().trim())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName().trim())
                .email(dto.getEmail() != null ? dto.getEmail().trim() : null)
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .role(dto.getRole())
                .status(dto.getStatus() != null ? dto.getStatus() : Enums.Status.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public AppUser updateUser(Long id, UserDto dto) {
        AppUser user = getUserById(id);

        if (!user.getUsername().equalsIgnoreCase(dto.getUsername())
                && userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + dto.getUsername());
        }

        user.setUsername(dto.getUsername().trim());
        user.setFullName(dto.getFullName().trim());
        user.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        user.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
        user.setRole(dto.getRole());
        user.setStatus(dto.getStatus());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void toggleStatus(Long id) {
        AppUser user = getUserById(id);
        user.setStatus(user.getStatus() == Enums.Status.ACTIVE ? Enums.Status.INACTIVE : Enums.Status.ACTIVE);
        userRepository.save(user);
    }
}
