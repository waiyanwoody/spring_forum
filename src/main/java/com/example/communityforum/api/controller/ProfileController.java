package com.example.communityforum.api.controller;

import com.example.communityforum.dto.file.FileUploadResponseDTO;
import com.example.communityforum.dto.user.ProfileRequest;
import com.example.communityforum.dto.user.ProfileResponseDTO;
import com.example.communityforum.dto.user.ProfileStatsDTO;
import com.example.communityforum.exception.FileValidationException;
import com.example.communityforum.exception.PermissionDeniedException;
import com.example.communityforum.exception.ResourceNotFoundException;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.UserRepository;
import com.example.communityforum.security.SecurityUtils;
import com.example.communityforum.service.FileStorageService;
import com.example.communityforum.service.ProfileService;
import com.example.communityforum.service.UserService;
import com.example.communityforum.service.VerificationService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Profiles", description = "Endpoints for managing forum user profiles")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;
    private final SecurityUtils securityUtils;

    private final FileStorageService fileStorageService;

    private final long MAX_AVATAR_SIZE = 2 * 1024 * 1024; // 2 MB, can also come from app.properties
    private final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/jpg" , "image/gif");
    private final UserService userService;
    private final UserRepository userRepository;
    private final VerificationService verificationService;

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

    // Update profile
    @PutMapping
    public ResponseEntity<ProfileResponseDTO> updateProfile(@Valid @RequestBody ProfileRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        ProfileResponseDTO updatedProfile = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    // Upload profile avatar
    @PostMapping("/avatar")
    public ResponseEntity<FileUploadResponseDTO> uploadAvatar(
            @RequestParam("avatar") MultipartFile avatar,
            @RequestParam(value = "userId", required = false) Long userId // optional for admin
    ) {
        // 1. Validate the image
        validateImage(avatar, MAX_AVATAR_SIZE, ALLOWED_TYPES);

        // 2. Get current user
        User currentUser = securityUtils.getCurrentUser();

        // 3. Determine target user
        Long targetUserId;
        if (userId != null) {
            if (!currentUser.getRole().equals("ADMIN")) {
                throw new PermissionDeniedException("You are not allowed to upload for other users");
            }
            targetUserId = userId;
        } else {
            targetUserId = currentUser.getId();
        }

        // 4. Get target user entity
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        // 5. Delete existing avatar if exists
        if (targetUser.getAvatarPath() != null && !targetUser.getAvatarPath().isEmpty()) {
            fileStorageService.deleteFile(targetUser.getAvatarPath());
        }


        // 7. Upload new file (returns relative path)
        String folder = "avatars";
        String relativePath = fileStorageService.upload(avatar, folder);
        String safeFileName = extractFileName(relativePath);
        // saved as avatars/<filename>

        // 8. Build download URL for client
        String downloadUrl = fileStorageService.buildFileUrl(relativePath);

        // 9. Update user entity with relative path
        userService.updateAvatar(targetUserId, relativePath);

        // 10. Return response
        FileUploadResponseDTO response = new FileUploadResponseDTO(safeFileName, relativePath, downloadUrl);
        return ResponseEntity.ok(response);
    }


    // helper function to get file name
    private String extractFileName(String path){
        return path.substring(path.lastIndexOf('/') + 1);
    }

    // Image validation for profile
    private void validateImage(MultipartFile file, long maxSize, List<String> allowedTypes) {
        if (file.isEmpty()) {
            throw new FileValidationException("File is empty");
        }

        if (file.getSize() > maxSize) {
            throw new FileValidationException("File size exceeds limit");
        }

        if (!allowedTypes.contains(file.getContentType())) {
            throw new FileValidationException("Only JPEG, PNG, and GIF are allowed");
        }

        String fileName = file.getOriginalFilename();
        if (!fileName.matches(".*\\.(jpg|jpeg|png|gif)$")) {
            throw new FileValidationException("Invalid file extension");
        }
    }

    // Get profile statistics
    @GetMapping("/{id}/stats")
    public ResponseEntity<ProfileStatsDTO> getUserStats(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getProfileStats(id));
    }

    // change new email
    @PostMapping("/change-email")
    public ResponseEntity<Void> requestEmailChange(@RequestParam String newEmail) {
        User current = securityUtils.getCurrentUser();
        verificationService.startEmailChange(current, newEmail, true); // set false to keep access
        return ResponseEntity.accepted().build();
    }
}
