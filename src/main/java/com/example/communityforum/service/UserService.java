package com.example.communityforum.service;

import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //create new user
    public User createUser(User user) {
        //validation here
        return userRepository.save(user);
    }

    //get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    //get user by ID
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    // Get user by email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Get user by username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Update user
//    public User updateUser(Long id, User userDetails) {
//        return userRepository.findById(id)
//                .map(user -> {
//                    user.setUsername(userDetails.getUsername());
//                    user.setEmail(userDetails.getEmail());
//                    user.setPassword(userDetails.getPassword()); // Make sure to hash password if needed
//                    // set other fields as needed
//                    return userRepository.save(user);
//                })
//                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
//    }

    // Delete user
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


}
