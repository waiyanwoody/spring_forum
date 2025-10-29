package com.example.communityforum.api.controller;

import com.example.communityforum.dto.auth.AuthRequest;
import com.example.communityforum.dto.auth.AuthResponse;
import com.example.communityforum.dto.user.UserRequestDTO;
import com.example.communityforum.dto.user.UserResponseDTO;
import com.example.communityforum.exception.DuplicateResourceException;
import com.example.communityforum.exception.HttpStatusException;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.UserRepository;
import com.example.communityforum.security.JwtUtil;
import com.example.communityforum.security.SecurityUtils;
import com.example.communityforum.service.VerificationService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.nio.charset.StandardCharsets;

import org.apache.catalina.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

@Tag(name = "Authentication", description = "Endpoints authentication")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${frontend.url}")
    private String frontendUrl;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private VerificationService verificationService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        User currentUser = securityUtils.getCurrentUser(); // fetch logged-in user

        // Map to DTO (you can use a mapper if available)
        UserResponseDTO userDTO = UserResponseDTO.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .email(currentUser.getEmail())
                .role(currentUser.getRole())
                .build();

        return ResponseEntity.ok(userDTO);
    }

    // -- Register --
    @PostMapping("/register")
    public AuthResponse registerUser(@Valid @RequestBody UserRequestDTO request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }
        // create and save new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole("USER");
        user.setEmailVerified(false);
        userRepository.save(user);

        // Send verification email
        verificationService.sendVerification(user);

        String token = jwtUtil.generateToken(
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRole())
                        .build());

        return new AuthResponse(token);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmailRedirect(@RequestParam String token) {
        String redirectUrl;

        try {
            verificationService.verify(token);
            redirectUrl = frontendUrl + "/verified?status=ok";
        } catch (HttpStatusException e) {
            redirectUrl = frontendUrl + "/verified?status=fail&reason=" +
                    UriUtils.encode(e.getMessage(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            redirectUrl = frontendUrl + "/verified?status=fail&reason=" +
                    UriUtils.encode("Something went wrong", StandardCharsets.UTF_8);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", redirectUrl);
        return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302 redirect
    }

    // -- Login --
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token);
    }

}
