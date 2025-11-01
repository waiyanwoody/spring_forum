package com.example.communityforum;

import com.example.communityforum.persistence.entity.*;
import com.example.communityforum.persistence.repository.*;
import com.github.javafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;
    private final Faker faker = new Faker();

    public DataSeeder(UserRepository userRepository,
                      PostRepository postRepository,
                      CommentRepository commentRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private String safeSubstring(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }

    @Override
    public void run(String... args) {
        // ✅ Step 1: Ensure admin exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .fullname("admin")
                    .username("admin")
                    .email("admin@example.com")
                    .emailVerified(true)
                    .password(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .bio("System administrator account")
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Admin account created: admin@example.com (password: admin123)");
        } else {
            System.out.println("ℹ️ Admin account already exists.");
        }

        // ✅ Step 2: Seed test data only if DB is empty (excluding admin)
        if (userRepository.count() <= 1) { // only admin exists
            System.out.println("🌱 Seeding large test data...");

            // --- Users ---
            List<User> users = IntStream.range(0, 10)
                    .mapToObj(i -> {
                        User u = new User();
                        u.setFullname(faker.name().fullName());
                        u.setUsername(safeSubstring(faker.name().username(), 50));
                        u.setEmail(safeSubstring(faker.internet().emailAddress(), 100));
                        u.setPassword(passwordEncoder.encode("password123"));
                        return u;
                    }).collect(Collectors.toList());
            userRepository.saveAll(users);

            // --- Posts ---
            List<Post> posts = IntStream.range(0, 50)
                    .mapToObj(i -> {
                        Post p = new Post();
                        p.setTitle(safeSubstring(faker.book().title(), 200));

                        // Generate 3-7 paragraphs of content
                        List<String> paragraphs = faker.lorem().paragraphs(faker.number().numberBetween(3, 8));
                        String content = String.join("\n\n", paragraphs); // separate paragraphs by new lines

                        p.setContent(safeSubstring(content, 5000)); // increase maxLength to allow longer content
                        p.setUser(users.get(faker.number().numberBetween(0, users.size())));
                        return p;
                    }).collect(Collectors.toList());
            postRepository.saveAll(posts);

            // --- Comments ---
            List<Comment> comments = IntStream.range(0, 100)
                    .mapToObj(i -> {
                        Comment c = new Comment();
                        c.setText(safeSubstring(faker.lorem().sentence(3, 8), 500));
                        c.setUser(users.get(faker.number().numberBetween(0, users.size())));
                        c.setPost(posts.get(faker.number().numberBetween(0, posts.size())));
                        return c;
                    }).collect(Collectors.toList());
            commentRepository.saveAll(comments);

            System.out.println("✅ Seeding complete: 1 admin, 10 users, 50 posts, 100 comments!");
        } else {
            System.out.println("ℹ️ Data already exists. Skipping seed.");
        }
    }
}
