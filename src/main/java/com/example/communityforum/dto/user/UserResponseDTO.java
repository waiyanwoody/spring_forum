package com.example.communityforum.dto.user;

import com.example.communityforum.persistence.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String created_at;
    private boolean email_verified;
    private String email_verified_at;

    public UserResponseDTO(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public static UserResponseDTO fromEntity(User user) {

        UserResponseDTO dto = new UserResponseDTO();

        // Map fields from User entity to UserResponseDTO
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        return dto;

    }

}
