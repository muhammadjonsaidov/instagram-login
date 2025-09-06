package com.instagram.businessdiscovery.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instagram.businessdiscovery.domain.BusinessDiscoverySearch;
import com.instagram.businessdiscovery.domain.User;
import com.instagram.businessdiscovery.dto.BusinessDiscoveryDto;
import com.instagram.businessdiscovery.repository.BusinessDiscoverySearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessDiscoveryService {

    private final BusinessDiscoverySearchRepository searchRepository;
    private final InstagramApiService instagramApiService;
    private final ObjectMapper objectMapper;

    @Value("${instagram.api.business-discovery.rate-limit:200}")
    private int hourlyRateLimit;

    /**
     * Perform business discovery search for a target username
     */
    @Transactional
    public Mono<BusinessDiscoveryDto> searchBusinessAccount(User searcherUser, String targetUsername, boolean includeMedia) {
        log.info("User {} searching for business account: {}", searcherUser.getUsername(), targetUsername);
        
        // Check rate limiting
        return checkRateLimit(searcherUser)
                .flatMap(allowed -> {
                    if (!allowed) {
                        return Mono.error(new RuntimeException("Rate limit exceeded. Maximum " + hourlyRateLimit + " searches per hour."));
                    }
                    
                    // Perform the search
                    Mono<BusinessDiscoveryDto> searchMono = includeMedia ?
                            instagramApiService.getBusinessDiscoveryWithMedia(searcherUser.getInstagramId(), targetUsername, searcherUser.getAccessToken()) :
                            instagramApiService.getBusinessDiscovery(searcherUser.getInstagramId(), targetUsername, searcherUser.getAccessToken());
                    
                    return searchMono
                            .flatMap(result -> saveSearchResult(searcherUser, targetUsername, result, BusinessDiscoverySearch.SearchStatus.SUCCESS, null))
                            .onErrorResume(error -> {
                                log.error("Business discovery failed for target {}: {}", targetUsername, error.getMessage());
                                return saveSearchResult(searcherUser, targetUsername, null, BusinessDiscoverySearch.SearchStatus.FAILED, error.getMessage())
                                        .then(Mono.error(error));
                            });
                });
    }

    /**
     * Get search history for a user
     */
    @Transactional(readOnly = true)
    public List<BusinessDiscoverySearch> getSearchHistory(User user) {
        return searchRepository.findBySearcherUserOrderByCreatedAtDesc(user);
    }

    /**
     * Get recent searches (last 24 hours)
     */
    @Transactional(readOnly = true)
    public List<BusinessDiscoverySearch> getRecentSearches(User user) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return searchRepository.findRecentSearchesByUser(user, since);
    }

    /**
     * Get searches for a specific target username
     */
    @Transactional(readOnly = true)
    public List<BusinessDiscoverySearch> getSearchesForTarget(String targetUsername) {
        return searchRepository.findByTargetUsernameOrderByCreatedAtDesc(targetUsername);
    }

    /**
     * Check if user has exceeded rate limit
     */
    private Mono<Boolean> checkRateLimit(User user) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        Long searchCount = searchRepository.countSearchesByUserSince(user, oneHourAgo);
        
        boolean allowed = searchCount < hourlyRateLimit;
        log.debug("Rate limit check for user {}: {}/{} searches in last hour", 
                user.getUsername(), searchCount, hourlyRateLimit);
        
        return Mono.just(allowed);
    }

    /**
     * Save search result to database
     */
    private Mono<BusinessDiscoveryDto> saveSearchResult(User searcherUser, String targetUsername, 
                                                       BusinessDiscoveryDto result, 
                                                       BusinessDiscoverySearch.SearchStatus status, 
                                                       String errorMessage) {
        try {
            BusinessDiscoverySearch search = BusinessDiscoverySearch.builder()
                    .searcherUser(searcherUser)
                    .targetUsername(targetUsername)
                    .searchStatus(status)
                    .errorMessage(errorMessage)
                    .build();

            if (result != null) {
                search.setTargetInstagramId(result.getId());
                search.setSearchResult(objectMapper.writeValueAsString(result));
            }

            searchRepository.save(search);
            log.debug("Saved search result for target: {} with status: {}", targetUsername, status);
            
            return result != null ? Mono.just(result) : Mono.empty();
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize search result: {}", e.getMessage());
            return Mono.just(result);
        }
    }

    /**
     * Get cached search result if available and recent
     */
    @Transactional(readOnly = true)
    public Optional<BusinessDiscoveryDto> getCachedResult(User searcherUser, String targetUsername) {
        Optional<BusinessDiscoverySearch> recentSearch = searchRepository
                .findBySearcherUserAndTargetUsername(searcherUser, targetUsername);
        
        if (recentSearch.isPresent()) {
            BusinessDiscoverySearch search = recentSearch.get();
            
            // Check if result is recent (less than 1 hour old) and successful
            if (search.getSearchStatus() == BusinessDiscoverySearch.SearchStatus.SUCCESS &&
                search.getCreatedAt().isAfter(LocalDateTime.now().minusHours(1)) &&
                search.getSearchResult() != null) {
                
                try {
                    BusinessDiscoveryDto result = objectMapper.readValue(search.getSearchResult(), BusinessDiscoveryDto.class);
                    log.debug("Returning cached result for target: {}", targetUsername);
                    return Optional.of(result);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to deserialize cached result for target: {}", targetUsername);
                }
            }
        }
        
        return Optional.empty();
    }

    /**
     * Get search statistics for a user
     */
    @Transactional(readOnly = true)
    public SearchStatistics getSearchStatistics(User user) {
        List<BusinessDiscoverySearch> allSearches = searchRepository.findBySearcherUserOrderByCreatedAtDesc(user);
        
        long totalSearches = allSearches.size();
        long successfulSearches = allSearches.stream()
                .filter(search -> search.getSearchStatus() == BusinessDiscoverySearch.SearchStatus.SUCCESS)
                .count();
        long failedSearches = totalSearches - successfulSearches;
        
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentSearches = searchRepository.countSearchesByUserSince(user, oneHourAgo);
        
        return SearchStatistics.builder()
                .totalSearches(totalSearches)
                .successfulSearches(successfulSearches)
                .failedSearches(failedSearches)
                .recentSearches(recentSearches)
                .remainingSearches(Math.max(0, hourlyRateLimit - recentSearches))
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class SearchStatistics {
        private long totalSearches;
        private long successfulSearches;
        private long failedSearches;
        private long recentSearches;
        private long remainingSearches;
    }
}