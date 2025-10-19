package com.example.communityforum.api.controller;

import com.example.communityforum.dto.user.ProfileResponseDTO;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.security.SecurityUtils;
import com.example.communityforum.service.ProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Profiles", description = "Endpoints for managing forum user profiles")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;
    private final SecurityUtils securityUtils;

    // Get current user profile
    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDTO> getMyProfile() {
        User currentUser =  securityUtils.getCurrentUser();
        return ResponseEntity.ok(profileService.getCurrentUserProfile(currentUser));
    }

    // Get any user profile (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponseDTO> getUserProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getUserProfileById(id));
    }

}
