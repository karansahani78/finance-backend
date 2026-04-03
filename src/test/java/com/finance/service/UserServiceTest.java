package com.finance.service;

import com.finance.dto.request.CreateUserRequest;
import com.finance.dto.response.UserResponse;
import com.finance.exception.DuplicateResourceException;
import com.finance.model.User;
import com.finance.model.enums.Role;
import com.finance.model.enums.UserStatus;
import com.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@finance.io")
                .password("hashed")
                .role(Role.VIEWER)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createUserSuccessfully() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Test User");
        request.setEmail("test@finance.io");
        request.setPassword("securepass");
        request.setRole(Role.VIEWER);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserResponse response = userService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@finance.io");
        assertThat(response.getRole()).isEqualTo(Role.VIEWER);

        verify(userRepository).existsByEmail("test@finance.io");
        verify(passwordEncoder).encode("securepass");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserThrowsWhenEmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("existing@finance.io");
        request.setPassword("pass");
        request.setRole(Role.VIEWER);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void getByIdReturnsCorrectUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        UserResponse response = userService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test User");
    }

    @Test
    void softDeleteSetsDeletedAtAndInactivates() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        userService.softDelete(1L);

        assertThat(mockUser.getDeletedAt()).isNotNull();
        assertThat(mockUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(userRepository).save(mockUser);
    }
}
