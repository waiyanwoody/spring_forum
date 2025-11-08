package com.example.communityforum.integration;

import com.example.communityforum.CommunityForumApplication;
import com.example.communityforum.dto.user.UserRequestDTO;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("ci")
@SpringBootTest(classes = CommunityForumApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;

    @BeforeEach
    void setUp() {
        // Clear database
        userRepository.deleteAll();

        // Create only admin user
        User admin = new User();
        admin.setFullname("Admin");
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(new BCryptPasswordEncoder().encode("password"));
        admin.setRole("ADMIN");
        userRepository.save(admin);

        adminToken = jwtUtil.generateToken(admin);
    }

    @Test
    void testGetAllUsers_WithValidJWT() {
        String url = "http://localhost:" + port + "/api/admin/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<UserResponseDTO[]> response =
                restTemplate.exchange(url, HttpMethod.GET, request, UserResponseDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1); // only admin
    }

    @Test
    void testCreateUser_WithValidJWT() {
        String url = "http://localhost:" + port + "/api/admin/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        UserRequestDTO newUser = new UserRequestDTO();
        newUser.setUsername("bob");
        newUser.setEmail("bob@example.com");
        newUser.setPassword("password");

        HttpEntity<UserRequestDTO> request = new HttpEntity<>(newUser, headers);

        ResponseEntity<UserResponseDTO> response =
                restTemplate.postForEntity(url, request, UserResponseDTO.class);

        // Validate response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("bob");
        assertThat(response.getBody().getEmail()).isEqualTo("bob@example.com");

        // Validate DB
        List<UserResponseDTO> users = userRepository.findAll()
                .stream()
                .map(u -> new UserResponseDTO(u.getId(), u.getUsername(), u.getEmail()))
                .toList();

        assertThat(users).hasSize(2); // admin + bob
    }

    @Test
    void testDeleteUser_WithValidJWT() {
        // First create user to delete
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword(new BCryptPasswordEncoder().encode("password"));
        user.setRole("USER");
        userRepository.save(user);

        String url = "http://localhost:" + port + "/api/admin/users/" + user.getId();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(userRepository.existsById(user.getId())).isFalse();
    }

    @Test
    void testGetUserById_WithValidJWT() {
        // First create user to fetch
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword(new BCryptPasswordEncoder().encode("password"));
        user.setRole("USER");
        userRepository.save(user);

        String url = "http://localhost:" + port + "/api/admin/users/" + user.getId();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<UserResponseDTO> response =
                restTemplate.exchange(url, HttpMethod.GET, request, UserResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUsername()).isEqualTo("alice");
    }

    @Test
    void testGetUserById_WithInvalidJWT_ShouldReturn401() {
        // First create user to fetch
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword(new BCryptPasswordEncoder().encode("password"));
        user.setRole("USER");
        userRepository.save(user);

        String url = "http://localhost:" + port + "/api/admin/users/" + user.getId();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid-token");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
