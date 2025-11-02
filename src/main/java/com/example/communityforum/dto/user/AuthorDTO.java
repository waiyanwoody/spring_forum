package com.example.communityforum.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public class AuthorDTO {
        private Long id;
        private String username;

        @JsonProperty("avatar_path")
        private String avatar_path;

        // constructors
        public AuthorDTO() {}
        public AuthorDTO(Long id, String username, String avatar_path) {
            this.id = id;
            this.username = username;
            this.avatar_path = avatar_path;
        }

        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getAvatarPath() { return avatar_path; }
        public void setAvatarPath(String avatarPath) { this.avatar_path = avatarPath; }
}