package com.example.instagramlogin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder // Ob'ektni oson yaratish uchun
public class UserResponseDTO {
    private Long internalId;
    private String instagramId;
    private String username;
    private String fullName;
    private String profilePictureUrl;
    private String biography;

    private MediaDTO media;
}