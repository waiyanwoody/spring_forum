package com.example.communityforum.repository;

import com.example.communityforum.persistence.repository.UserRepository;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.projection.ProfileCountsProjection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ActiveProfiles("ci")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void getProfileCounts_success()
    {
        // save test data
        User user = new User();
        user.setFullname("test");
        user.setUsername("test");
        user.setEmail("test");
        user.setPassword("test");
        userRepository.save(user);

        // example projection test
        ProfileCountsProjection projection = userRepository.getProfileCounts(user.getId());
        assertNotNull(projection);
        assertEquals(0,projection.getPostCount());
    }

}
