package com.instagram.businessdiscovery.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "instagram_id", unique = true, nullable = false)
    private String instagramId;
    
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    
    @Column(name = "full_name")
    private String fullName;
    
    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;
    
    @Column(name = "profile_picture_url", columnDefinition = "TEXT")
    private String profilePictureUrl;
    
    @Column(name = "followers_count")
    private Integer followersCount;
    
    @Column(name = "follows_count")
    private Integer followsCount;
    
    @Column(name = "media_count")
    private Integer mediaCount;
    
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;
    
    /*@Column(name = "is_business_account")
    private Boolean isBusinessAccount;*/
    /*
    @Column(name = "account_type")
    private String accountType;*/
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}