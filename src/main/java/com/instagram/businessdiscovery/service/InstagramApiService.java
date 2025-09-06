package com.instagram.businessdiscovery.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.instagram.businessdiscovery.dto.BusinessDiscoveryDto;
import com.instagram.businessdiscovery.dto.InstagramProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

@Service
@Slf4j
public class InstagramApiService {

    private final WebClient webClient;

    @Value("${facebook.client-id}")
    private String clientId;

    @Value("${facebook.client-secret}")
    private String clientSecret;

    @Value("${facebook.redirect-uri}")
    private String redirectUri;

    @Value("${facebook.api.token-uri}")
    private String tokenUri;

    @Value("${facebook.api.me-accounts-uri}")
    private String meAccountsUri;

    @Value("${instagram.api.base-url}")
    private String instagramApiBaseUrl;

    public InstagramApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    /**
     * Exchange authorization code for access token
     */
    public Mono<String> getAccessToken(String code) {
        log.debug("Exchanging authorization code for access token");

        return webClient.get()
                .uri(tokenUri, uriBuilder -> uriBuilder
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("redirect_uri", redirectUri)
                        .queryParam("code", code)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> response.get("access_token").asText())
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(token -> log.debug("Successfully obtained access token"))
                .doOnError(error -> log.error("Failed to get access token: {}", error.getMessage()));
    }

    /**
     * Get Instagram Business Account ID from Facebook Pages
     */
    public Mono<String> getInstagramBusinessAccountId(String accessToken) {
        log.debug("Getting Instagram Business Account ID");

        return webClient.get()
                .uri(meAccountsUri, uriBuilder -> uriBuilder
                        .queryParam("access_token", accessToken)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(response -> {
                    JsonNode data = response.get("data");
                    if (data == null || !data.isArray() || data.isEmpty()) {
                        return Mono.error(new RuntimeException("No Facebook Pages found for this user"));
                    }

                    String firstPageId = data.get(0).get("id").asText();
                    return getInstagramAccountFromPage(firstPageId, accessToken);
                })
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(accountId -> log.debug("Successfully obtained Instagram account ID: {}", accountId))
                .doOnError(error -> log.error("Failed to get Instagram account ID: {}", error.getMessage()));
    }

    private Mono<String> getInstagramAccountFromPage(String pageId, String accessToken) {
        return webClient.get()
                .uri(instagramApiBaseUrl + "/" + pageId, uriBuilder -> uriBuilder
                        .queryParam("fields", "instagram_business_account")
                        .queryParam("access_token", accessToken)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(response -> {
                    JsonNode igAccount = response.get("instagram_business_account");
                    if (igAccount != null && igAccount.has("id")) {
                        return Mono.just(igAccount.get("id").asText());
                    } else {
                        return Mono.error(new RuntimeException("This Facebook Page is not connected to an Instagram Business Account"));
                    }
                });
    }

    /**
     * Get user's own Instagram profile
     */
    public Mono<InstagramProfileDto> getUserProfile(String instagramAccountId, String accessToken) {
        log.debug("Getting user profile for account: {}", instagramAccountId);

        String fields = "id,username,name,biography,followers_count,follows_count,media_count,profile_picture_url";

        URI uri = UriComponentsBuilder.fromUriString(instagramApiBaseUrl)
                .path("/" + instagramAccountId)
                .queryParam("fields", fields)
                .queryParam("access_token", accessToken)
                .build()
                .toUri();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(InstagramProfileDto.class)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(profile -> log.debug("Successfully retrieved profile for: {}", profile.getUsername()))
                .doOnError(error -> log.error("Failed to get user profile: {}", error.getMessage()));
    }

    /**
     * Business Discovery - Get other user's public business information
     */
    @Cacheable(value = "businessDiscovery", key = "#targetUsername")
    public Mono<BusinessDiscoveryDto> getBusinessDiscovery(String instagramAccountId, String targetUsername, String accessToken) {
        log.debug("Performing business discovery for target: {}", targetUsername);

        String fields = "id,username,name,biography,followers_count,follows_count,media_count,profile_picture_url,website";

        URI uri = UriComponentsBuilder.fromUriString(instagramApiBaseUrl)
                .path("/" + instagramAccountId)
                .queryParam("fields", "business_discovery.username(" + targetUsername + "){" + fields + "}")
                .queryParam("access_token", accessToken)
                .build()
                .toUri();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(this::parseBusinessDiscoveryResponse)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(discovery -> log.debug("Successfully retrieved business discovery for: {}", targetUsername))
                .doOnError(WebClientResponseException.class, error -> {
                    log.error("API Error for business discovery of {}: {} - {}",
                            targetUsername, error.getStatusCode(), error.getResponseBodyAsString());
                })
                .doOnError(error -> log.error("Failed to get business discovery for {}: {}", targetUsername, error.getMessage()));
    }

    private Mono<BusinessDiscoveryDto> parseBusinessDiscoveryResponse(JsonNode response) {
        JsonNode businessDiscovery = response.get("business_discovery");
        if (businessDiscovery == null) {
            return Mono.error(new RuntimeException("Business discovery data not found. User might not be a business account or username is incorrect."));
        }

        BusinessDiscoveryDto dto = BusinessDiscoveryDto.builder()
                .id(getTextValue(businessDiscovery, "id"))
                .username(getTextValue(businessDiscovery, "username"))
                .name(getTextValue(businessDiscovery, "name"))
                .biography(getTextValue(businessDiscovery, "biography"))
                .followersCount(getIntValue(businessDiscovery, "followers_count"))
                .followsCount(getIntValue(businessDiscovery, "follows_count"))
                .mediaCount(getIntValue(businessDiscovery, "media_count"))
                .profilePictureUrl(getTextValue(businessDiscovery, "profile_picture_url"))
                /*.accountType(getTextValue(businessDiscovery, "account_type"))*/
                .website(getTextValue(businessDiscovery, "website"))
                /*.isVerified(getBooleanValue(businessDiscovery))*/
                .build();

        return Mono.just(dto);
    }

    /**
     * Get recent media for business discovery target
     */
    public Mono<BusinessDiscoveryDto> getBusinessDiscoveryWithMedia(String instagramAccountId, String targetUsername, String accessToken) {
        log.debug("Getting business discovery with media for: {}", targetUsername);

        String mediaFields = "id,media_url,media_type,caption,like_count,comments_count,timestamp,permalink";
        String fields = "id,username,name,biography,followers_count,follows_count,media_count,profile_picture_url,website,media{" + mediaFields + "}";

        URI uri = UriComponentsBuilder.fromUriString(instagramApiBaseUrl)
                .path("/" + instagramAccountId)
                .queryParam("fields", "business_discovery.username(" + targetUsername + "){" + fields + "}")
                .queryParam("access_token", accessToken)
                .build()
                .toUri();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(this::parseBusinessDiscoveryWithMediaResponse)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(discovery -> log.debug("Successfully retrieved business discovery with media for: {}", targetUsername))
                .doOnError(error -> log.error("Failed to get business discovery with media for {}: {}", targetUsername, error.getMessage()));
    }

    private Mono<BusinessDiscoveryDto> parseBusinessDiscoveryWithMediaResponse(JsonNode response) {
        JsonNode businessDiscovery = response.get("business_discovery");
        if (businessDiscovery == null) {
            return Mono.error(new RuntimeException("Business discovery data not found"));
        }

        BusinessDiscoveryDto dto = BusinessDiscoveryDto.builder()
                .id(getTextValue(businessDiscovery, "id"))
                .username(getTextValue(businessDiscovery, "username"))
                .name(getTextValue(businessDiscovery, "name"))
                .biography(getTextValue(businessDiscovery, "biography"))
                .followersCount(getIntValue(businessDiscovery, "followers_count"))
                .followsCount(getIntValue(businessDiscovery, "follows_count"))
                .mediaCount(getIntValue(businessDiscovery, "media_count"))
                .profilePictureUrl(getTextValue(businessDiscovery, "profile_picture_url"))
                /*.accountType(getTextValue(businessDiscovery, "account_type"))*/
                .website(getTextValue(businessDiscovery, "website"))
                /*.isVerified(getBooleanValue(businessDiscovery))*/
                .build();

        // Parse media
        JsonNode media = businessDiscovery.get("media");
        if (media != null && media.has("data")) {
            JsonNode mediaData = media.get("data");
            java.util.List<BusinessDiscoveryDto.MediaInsightDto> mediaList = new java.util.ArrayList<>();

            for (JsonNode mediaItem : mediaData) {
                BusinessDiscoveryDto.MediaInsightDto mediaDto = BusinessDiscoveryDto.MediaInsightDto.builder()
                        .id(getTextValue(mediaItem, "id"))
                        .mediaUrl(getTextValue(mediaItem, "media_url"))
                        .mediaType(getTextValue(mediaItem, "media_type"))
                        .caption(getTextValue(mediaItem, "caption"))
                        .likeCount(getIntValue(mediaItem, "like_count"))
                        .commentsCount(getIntValue(mediaItem, "comments_count"))
                        .timestamp(getTextValue(mediaItem, "timestamp"))
                        .permalink(getTextValue(mediaItem, "permalink"))
                        .build();

                // Calculate engagement rate
                if (dto.getFollowersCount() != null && dto.getFollowersCount() > 0) {
                    int totalEngagement = (mediaDto.getLikeCount() != null ? mediaDto.getLikeCount() : 0) +
                            (mediaDto.getCommentsCount() != null ? mediaDto.getCommentsCount() : 0);
                    double engagementRate = (double) totalEngagement / dto.getFollowersCount() * 100;
                    mediaDto.setEngagementRate(engagementRate);
                }

                mediaList.add(mediaDto);
            }

            dto.setRecentMedia(mediaList);

            // Calculate insights
            dto.setInsights(calculateInsights(mediaList, dto.getFollowersCount()));
        }

        return Mono.just(dto);
    }

    private BusinessDiscoveryDto.AccountInsightsDto calculateInsights(java.util.List<BusinessDiscoveryDto.MediaInsightDto> mediaList, Integer followersCount) {
        if (mediaList.isEmpty() || followersCount == null || followersCount == 0) {
            return null;
        }

        long totalLikes = mediaList.stream()
                .mapToLong(media -> media.getLikeCount() != null ? media.getLikeCount() : 0)
                .sum();

        long totalComments = mediaList.stream()
                .mapToLong(media -> media.getCommentsCount() != null ? media.getCommentsCount() : 0)
                .sum();

        double averageEngagementRate = mediaList.stream()
                .filter(media -> media.getEngagementRate() != null)
                .mapToDouble(BusinessDiscoveryDto.MediaInsightDto::getEngagementRate)
                .average()
                .orElse(0.0);

        return BusinessDiscoveryDto.AccountInsightsDto.builder()
                .averageEngagementRate(averageEngagementRate)
                .totalLikes(totalLikes)
                .totalComments(totalComments)
                .postsLast30Days(mediaList.size())
                .build();
    }

    // Helper methods for safe JSON parsing
    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null && !field.isNull() ? field.asText() : null;
    }

    private Integer getIntValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null && !field.isNull() ? field.asInt() : null;
    }

}