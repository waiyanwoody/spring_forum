package com.example.communityforum.service;

import com.example.communityforum.dto.user.UserRequestDTO;
import com.example.communityforum.dto.user.UserResponseDTO;
import com.example.communityforum.exception.DuplicateResourceException;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_success() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("alice");
        dto.setEmail("alice@example.com");
        dto.setPassword("password");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("alice");
        savedUser.setEmail("alice@example.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDTO response = userService.createUser(dto);

        assertEquals(1L, response.getId());
        assertEquals("alice", response.getUsername());
        assertEquals("alice@example.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_duplicateUsername_throwsException() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("alice");
        dto.setEmail("alice@example.com");

        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<UserResponseDTO> result = userService.getUserById(1L);
        assertTrue(result.isPresent());
        assertEquals("alice", result.get().getUsername());
    }

    @Test
    void getUserByUsername_success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        Optional<UserResponseDTO> result = userService.getUserByUsername("alice");
        assertTrue(result.isPresent());
        assertEquals("alice", result.get().getUsername());
    }

    @Test
    void getUserByEmail_success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        Optional<UserResponseDTO> result = userService.getUserByEmail("alice@example.com");
        assertTrue(result.isPresent());
        assertEquals("alice", result.get().getUsername());
    }

    @Test
    void getUserById_notFound_returnsEmpty() {
        Optional<UserResponseDTO> result = userService.getUserById(1L);
        assertFalse(result.isPresent());
    }

    @Test
    void deleteUser_callsRepository() {
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }
}
