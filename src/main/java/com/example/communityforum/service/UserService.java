package com.example.communityforum.service;

import com.example.communityforum.dto.user.ProfileRequest;
import com.example.communityforum.dto.user.ProfileResponseDTO;
import com.example.communityforum.dto.user.UserRequestDTO;
import com.example.communityforum.dto.user.UserResponseDTO;
import com.example.communityforum.exception.DuplicateResourceException;
import com.example.communityforum.exception.ResourceNotFoundException;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // create new user
    public UserResponseDTO createUser(UserRequestDTO dto) {

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username is already in use");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email is already in use");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        User savedUser = userRepository.save(user);
        return new UserResponseDTO(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
    }

    //get all users
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponseDTO(user.getId(),user.getUsername(),user.getEmail()))
                .toList();
    }

    //get user by ID
    public Optional<UserResponseDTO> getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(u -> new UserResponseDTO(u.getId(),u.getUsername(),u.getEmail()));
    }

    // Get user by email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Get user by username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    // update profile 
    public ProfileResponseDTO updateProfile(Long userId, ProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String username = request.getUsername();
        String email = request.getEmail();
        if (!user.getUsername().equals(username) &&
                userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username is already in use");
        }

        if (!user.getEmail().equals(email) &&
                userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already in use");
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setBio(request.getBio());
        User updatedUser = userRepository.save(user);
        return mapToProfileResponseDTO(updatedUser);
    }

    // map to profile response dto
    public ProfileResponseDTO mapToProfileResponseDTO(User user) {
        // response without posts
        return new ProfileResponseDTO(user.getId(), user.getUsername(), user.getEmail(), user.getBio(),
                user.getAvatarPath(), user.getCreatedAt());
    }

    // update user avatar
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User",userId));
        user.setAvatarPath(avatarUrl);
        userRepository.save(user);
    }

    // Delete user
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


}
