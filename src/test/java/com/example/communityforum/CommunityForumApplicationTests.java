package com.example.communityforum;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.example.communityforum.persistence.repository.UserRepository;

@SpringBootTest
class CommunityForumApplicationTests {

    @MockBean
    private UserRepository userRepository; // Mocked so context loads without DB issues

    @Test
    void contextLoads() {
    }
}
