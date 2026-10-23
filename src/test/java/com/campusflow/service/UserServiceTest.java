package com.campusflow.service;

import com.campusflow.dto.UserRegistrationDto;
import com.campusflow.entity.User;
import com.campusflow.exception.DuplicateEmailException;
import com.campusflow.exception.ResourceNotFoundException;
import com.campusflow.repository.UserRepository;
import com.campusflow.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Should register new user successfully with encoded password")
    void testRegisterUser_Success() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("John Doe");
        dto.setEmail("john.doe@university.edu");
        dto.setPassword("SecretPass123");
        dto.setConfirmPassword("SecretPass123");

        when(userRepository.existsByEmailIgnoreCase("john.doe@university.edu")).thenReturn(false);
        when(passwordEncoder.encode("SecretPass123")).thenReturn("$2a$12$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        User result = userService.registerUser(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john.doe@university.edu", result.getEmail());
        assertEquals("John Doe", result.getFullName());
        assertEquals("$2a$12$hashedPassword", result.getPassword());
        assertTrue(result.isEnabled());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email already registered")
    void testRegisterUser_DuplicateEmail() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("Jane Doe");
        dto.setEmail("existing@university.edu");
        dto.setPassword("SecretPass123");
        dto.setConfirmPassword("SecretPass123");

        when(userRepository.existsByEmailIgnoreCase("existing@university.edu")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.registerUser(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when passwords do not match")
    void testRegisterUser_PasswordMismatch() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("Jane Doe");
        dto.setEmail("jane@university.edu");
        dto.setPassword("SecretPass123");
        dto.setConfirmPassword("DifferentPass");

        when(userRepository.existsByEmailIgnoreCase("jane@university.edu")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user email does not exist")
    void testFindByEmail_NotFound() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findByEmail("nonexistent@domain.com"));
    }
}
