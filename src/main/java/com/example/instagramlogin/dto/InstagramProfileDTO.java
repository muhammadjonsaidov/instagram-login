package com.example.instagramlogin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InstagramProfileDTO {
    private String id;
    private String username;
    private String name;
    private String biography;
    @JsonProperty("followers_count")
    private Integer followersCount;
    @JsonProperty("profile_picture_url")
    private String profilePictureUrl;

//    private MediaDTO media;
}