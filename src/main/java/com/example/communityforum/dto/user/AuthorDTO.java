package com.example.communityforum.dto.user;

import lombok.Builder;

@Builder
public class AuthorDTO {
        private Long id;
        private String username;
        private String avatarPath;

        // constructors
        public AuthorDTO() {}
        public AuthorDTO(Long id, String username, String avatarPath) {
            this.id = id;
            this.username = username;
            this.avatarPath = avatarPath;
        }

        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getAvatarPath() { return avatarPath; }
        public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
}