package com.example.communityforum;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.example.communityforum.persistence.repository.UserRepository;

@SpringBootTest
class CommunityForumApplicationTests {

    @MockBean
    private UserRepository userRepository; // Mocked to avoid real DB dependency

    @Test
    void contextLoads() {
        // Spring Boot context loads successfully
    }
}
