package com.instagram.businessdiscovery.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_discovery_searches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDiscoverySearch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "searcher_user_id", nullable = false)
    private User searcherUser;
    
    @Column(name = "target_username", nullable = false)
    private String targetUsername;
    
    @Column(name = "target_instagram_id")
    private String targetInstagramId;
    
    @Column(name = "search_result", columnDefinition = "TEXT")
    private String searchResult;
    
    @Column(name = "search_status")
    @Enumerated(EnumType.STRING)
    private SearchStatus searchStatus;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public enum SearchStatus {
        SUCCESS, FAILED, PENDING
    }
}