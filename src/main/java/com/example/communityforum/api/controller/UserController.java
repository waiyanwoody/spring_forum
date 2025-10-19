package com.example.communityforum.api.controller;

import com.example.communityforum.dto.user.UserRequestDTO;
import com.example.communityforum.dto.user.UserResponseDTO;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users", description = "Endpoints for managing forum users")
@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*") // Allow frontend access (optional)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Create a new user
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO  userRequestDTO) {
        return ResponseEntity.ok(userService.createUser(userRequestDTO));
    }

    // Get all users
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Get user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Get user by username
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Update user
//    @PutMapping("/{id}")
//    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
//        return userService.getUserById(id)
//                .map(existingUser -> {
//                    existingUser.setUsername(userDetails.getUsername());
//                    existingUser.setEmail(userDetails.getEmail());
//                    existingUser.setPassword(userDetails.getPassword());
//                    // Hash password here if needed
//                    User updated = userService.createUser(existingUser);
//                    return ResponseEntity.ok(updated);
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }

    // ✅ Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
