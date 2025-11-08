package com.example.communityforum.integration;

import com.example.communityforum.CommunityForumApplication;
import com.example.communityforum.dto.user.UserResponseDTO;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.UserRepository;
import com.example.communityforum.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("ci")
@SpringBootTest(classes = CommunityForumApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String token;

    @BeforeEach
    void setUp() {
        // Clear database for clean state
        userRepository.deleteAll();

        // Create test user with admin role
        User user = new User();
        user.setFullname("Admin");
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setPassword(new BCryptPasswordEncoder().encode("password")); // hash password
        user.setRole("ADMIN");
        userRepository.save(user);

        token = jwtUtil.generateToken(user); // uses same secret as app
    }

    @Test
    void testGetUserByUsername_WithValidJWT() {
        String url = "http://localhost:" + port + "/api/admin/users/username/admin";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token); // add Authorization: Bearer <token>
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<UserResponseDTO> response = restTemplate.exchange(url, HttpMethod.GET, request, UserResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("admin");
        assertThat(response.getBody().getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    void testGetUserByUsername_WithInvalidJWT_ShouldReturn401() {
        String url = "http://localhost:" + port + "/api/admin/users/username/alice";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid-token"); // invalid token
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testGetUserByUsername_WithoutJWT_ShouldReturn401() {
        String url = "http://localhost:" + port + "/api/admin/users/username/alice";

        ResponseEntity<String> response =
                restTemplate.getForEntity(url, String.class);

        // Expect unauthorized
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
