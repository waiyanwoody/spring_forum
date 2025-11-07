package com.example.communityforum.integration;

import com.example.communityforum.dto.user.UserResponseDTO;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("ci")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/admin/users";
        userRepository.deleteAll();

        // Insert test user
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setFullname("Test User");
        user.setPassword("password");
        userRepository.save(user);
    }

    @Test
    void testGetAllUsers() {
        ResponseEntity<UserResponseDTO[]> response = restTemplate.getForEntity(baseUrl, UserResponseDTO[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getUsername()).isEqualTo("testuser");
    }

    @Test
    void testGetUserById() {
        Long userId = userRepository.findAll().get(0).getId();
        ResponseEntity<UserResponseDTO> response = restTemplate.getForEntity(baseUrl + "/" + userId, UserResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
    }

    @Test
    void testDeleteUser() {
        Long userId = userRepository.findAll().get(0).getId();
        ResponseEntity<Void> response = restTemplate.exchange(baseUrl + "/" + userId, HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(userRepository.findById(userId)).isEmpty();
    }
}
