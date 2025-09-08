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
public class BusinessDiscoveryDto {
    
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
    
    private String website;
    
    /*@JsonProperty("is_verified")
    private Boolean isVerified;*/
    
    // Recent media posts
    private List<MediaInsightDto> recentMedia;
    
    // Account insights (if available)
    private AccountInsightsDto insights;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaInsightDto {
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

        @JsonProperty("saved_count")
        private Integer savedCount; // Yangi maydon

        private String timestamp;
        private String permalink;
        
        // Engagement metrics
        @JsonProperty("engagement_rate")
        private Double engagementRate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountInsightsDto {
        
        @JsonProperty("average_engagement_rate")
        private Double averageEngagementRate;
        
        @JsonProperty("total_likes")
        private Long totalLikes;

        @JsonProperty("total_saves")
        private long totalSaves; // Yangi maydon

        @JsonProperty("total_comments")
        private Long totalComments;
        
        @JsonProperty("posts_last_30_days")
        private Integer postsLast30Days;
        
        @JsonProperty("follower_growth_rate")
        private Double followerGrowthRate;
    }
}