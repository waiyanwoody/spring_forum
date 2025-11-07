package com.example.communityforum.repository;

import com.example.communityforum.persistence.entity.Follow;
import com.example.communityforum.persistence.entity.Like;
import com.example.communityforum.persistence.entity.Post;
import com.example.communityforum.persistence.repository.FollowRepository;
import com.example.communityforum.persistence.repository.LikeRepository;
import com.example.communityforum.persistence.repository.PostRepository;
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

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    // -----------------------------------------------------------
    // Test 1: basic zero-data test
    // -----------------------------------------------------------
    @Test
    void getProfileCounts_returnsZerosForNewUser() {
        // Create and save a new user
        User user = new User();
        user.setFullname("Test User");
        user.setUsername("test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        userRepository.save(user);

        // Execute repository query
        ProfileCountsProjection projection = userRepository.getProfileCounts(user.getId());

        // Assert all counts are 0
        assertNotNull(projection);
        assertEquals(0, projection.getFollowerCount());
        assertEquals(0, projection.getFollowingCount());
        assertEquals(0, projection.getPostCount());
        assertEquals(0, projection.getPostLikeCount());
    }

    // -----------------------------------------------------------
    //  Test 2: realistic data test !
    //  but shouldn't be written in repository test which focus on repository function unit not integration test
    // -----------------------------------------------------------
//    @Test
//    void getProfileCounts_withFollowersPostsAndLikes_returnsCorrectCounts() {
//        // Create user (main)
//        User user = new User();
//        user.setFullname("Main User");
//        user.setUsername("mainuser");
//        user.setEmail("main@example.com");
//        user.setPassword("pass");
//        userRepository.save(user);
//
//        // Create follower and following users
//        User follower = new User();
//        follower.setFullname("Follower");
//        follower.setUsername("follower");
//        follower.setEmail("follower@example.com");
//        follower.setPassword("pass");
//        userRepository.save(follower);
//
//        User following = new User();
//        following.setFullname("Following");
//        following.setUsername("following");
//        following.setEmail("following@example.com");
//        following.setPassword("pass");
//        userRepository.save(following);
//
//        // mainuser follows someone → +1 followingCount
//        Follow followingRel = new Follow();
//        followingRel.setFollower(user);
//        followingRel.setFollowing(following);
//        followRepository.save(followingRel);
//
//        // someone follows mainuser → +1 followerCount
//        Follow followerRel = new Follow();
//        followerRel.setFollower(follower);
//        followerRel.setFollowing(user);
//        followRepository.save(followerRel);
//
//        // mainuser creates a post → +1 postCount
//        Post post = new Post();
//        post.setUser(user);
//        post.setContent("Hello World");
//        post.setTitle("hello");
//        post.setSlug("hello-world");
//        postRepository.save(post);
//
//        // follower likes that post → +1 postLikeCount
//        Like like = new Like();
//        like.setUser(follower);
//        like.setPost(post);
//        likeRepository.save(like);
//
//        // Execute repository query
//        ProfileCountsProjection projection = userRepository.getProfileCounts(user.getId());
//
//        // Assertions — verify query correctness
//        assertNotNull(projection);
//        assertEquals(1, projection.getFollowerCount());
//        assertEquals(1, projection.getFollowingCount());
//        assertEquals(1, projection.getPostCount());
//        assertEquals(1, projection.getPostLikeCount());
//    }

}
