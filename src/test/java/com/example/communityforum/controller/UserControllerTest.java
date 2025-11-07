package com.example.communityforum.controller;

import com.example.communityforum.api.controller.UserController;
import com.example.communityforum.dto.user.UserResponseDTO;
import com.example.communityforum.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @Test
    void testGetUserByUsername_UserExists() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        when(userService.getUserByUsername("alice"))
                .thenReturn(Optional.of(new UserResponseDTO(1L, "alice", "alice@example.com")));

        mockMvc.perform(get("/api/admin/users/username/alice"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetUserByUsername_UserNotExists() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        when(userService.getUserByUsername("unknown"))
        .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/users/username/unknown"))
                .andExpect(status().isNotFound());
    }
}

