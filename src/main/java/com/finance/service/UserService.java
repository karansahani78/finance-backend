package com.finance.service;

import com.finance.dto.request.CreateUserRequest;
import com.finance.dto.request.UpdateUserRequest;
import com.finance.dto.response.PageResponse;
import com.finance.dto.response.UserResponse;
import com.finance.exception.BusinessException;
import com.finance.exception.DuplicateResourceException;
import com.finance.exception.ResourceNotFoundException;
import com.finance.model.User;
import com.finance.model.enums.UserStatus;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return PageResponse.of(userRepository.searchUsers(search.trim(), pageable), UserResponse::from);
        }
        return PageResponse.of(userRepository.findAll(pageable), UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return UserResponse.from(fetchOrThrow(id));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = fetchOrThrow(id);

        if (request.getName() != null) user.setName(request.getName());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getStatus() != null) user.setStatus(request.getStatus());

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void softDelete(Long id) {
        User user = fetchOrThrow(id);
        if (user.getDeletedAt() != null) {
            throw new BusinessException("User is already deleted");
        }
        user.setDeletedAt(LocalDateTime.now());
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    private User fetchOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.user(id));
    }
}
