package com.instagram.businessdiscovery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstagramProfileDto {
    
    private String id;
    private String username;
    private String name;
    private String biography;
    
    @JsonProperty("followers_count")
    private Integer followersCount;
    
    @JsonProperty("follows_count")
    private Integer followsCount;
    
    @JsonProperty("media_count")
    private Integer mediaCount;
    
    @JsonProperty("profile_picture_url")
    private String profilePictureUrl;
    
    /*@JsonProperty("account_type")
    private String accountType;*/
    
    private List<InstagramMediaDto> media;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstagramMediaDto {
        private String id;
        
        @JsonProperty("media_url")
        private String mediaUrl;
        
        @JsonProperty("media_type")
        private String mediaType;
        
        private String caption;
        
        @JsonProperty("like_count")
        private Integer likeCount;
        
        @JsonProperty("comments_count")
        private Integer commentsCount;
        
        private String timestamp;
        private String permalink;
    }
}