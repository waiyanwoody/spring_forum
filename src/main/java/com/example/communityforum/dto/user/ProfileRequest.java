package com.example.communityforum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ProfileRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be 3-20 characters")
    private String username;

    @Size(max = 1000, message = "Bio must be at most 1000 characters")
    private String bio;

    @Size(max = 512, message = "Avatar path must be at most 512 characters")
    private String avatarPath;

    // Optional password change
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;

    private String currentPassword;

    public boolean isPasswordChangeRequested() {
        return newPassword != null && !newPassword.isBlank();
    }
}
