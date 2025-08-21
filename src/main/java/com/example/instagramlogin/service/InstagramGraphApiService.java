package com.example.instagramlogin.service;

import com.example.instagramlogin.model.FacebookAccessTokenResponse;
import com.example.instagramlogin.model.FacebookPagesResponse;
import com.example.instagramlogin.model.InstagramUserProfile;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class InstagramGraphApiService {

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
    @Value("${instagram.api.user-profile-uri}")
    private String userProfileUri;

    public InstagramGraphApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<String> getAccessToken(String code) {
        return webClient.get()
                .uri(tokenUri, uriBuilder -> uriBuilder
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("redirect_uri", redirectUri)
                        .queryParam("code", code)
                        .build())
                .retrieve()
                .bodyToMono(FacebookAccessTokenResponse.class)
                .map(FacebookAccessTokenResponse::getAccessToken);
    }

    public Mono<String> getInstagramAccountId(String accessToken) {
        return webClient.get()
                .uri(meAccountsUri, uriBuilder -> uriBuilder
                        .queryParam("access_token", accessToken)
                        .build())
                .retrieve()
                .bodyToMono(FacebookPagesResponse.class)
                .flatMap(response -> {
                    if (response.getData() == null || response.getData().isEmpty()) {
                        return Mono.error(new RuntimeException("User does not manage any Facebook Pages."));
                    }
                    String firstPageId = response.getData().getFirst().getId();

                    return webClient.get()
                            .uri(userProfileUri + firstPageId, uriBuilder -> uriBuilder
                                    .queryParam("fields", "instagram_business_account")
                                    .queryParam("access_token", accessToken)
                                    .build())
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .flatMap(jsonNode -> {
                                if (jsonNode.has("instagram_business_account") && jsonNode.get("instagram_business_account").has("id")) {
                                    String instagramId = jsonNode.get("instagram_business_account").get("id").asText();
                                    return Mono.just(instagramId);
                                } else {
                                    return Mono.error(new RuntimeException("The first page ('" + response.getData().getFirst().getName() + "') is not connected to an Instagram Business Account."));
                                }
                            });
                });
    }

    public Mono<InstagramUserProfile> getUserProfile(String instagramAccountId, String accessToken) {
        String fields = "id,username,name,biography,followers_count,profile_picture_url";
        String uri = userProfileUri + instagramAccountId;

        return webClient.get()
                .uri(uri, uriBuilder -> uriBuilder
                        .queryParam("fields", fields)
                        .queryParam("access_token", accessToken)
                        .build())
                .retrieve()
                .bodyToMono(InstagramUserProfile.class);
    }
}