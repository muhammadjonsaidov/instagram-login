package com.example.instagramlogin.service;

import com.example.instagramlogin.dto.InstagramProfileDTO;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

record FacebookAccessTokenResponse(String access_token) {
}

record FacebookPagesResponse(java.util.List<Page> data) {
}

record Page(String id, String name) {
}

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
                .retrieve().bodyToMono(FacebookAccessTokenResponse.class)
                .map(FacebookAccessTokenResponse::access_token);
    }

    public Mono<String> getInstagramAccountId(String accessToken) {
        return webClient.get()
                .uri(meAccountsUri, uriBuilder -> uriBuilder.queryParam("access_token", accessToken).build())
                .retrieve().bodyToMono(FacebookPagesResponse.class)
                .flatMap(response -> {
                    if (response.data() == null || response.data().isEmpty()) {
                        return Mono.error(new RuntimeException("User does not manage any Facebook Pages."));
                    }
                    String firstPageId = response.data().getFirst().id();
                    return webClient.get()
                            .uri(userProfileUri + firstPageId, uriBuilder -> uriBuilder
                                    .queryParam("fields", "instagram_business_account")
                                    .queryParam("access_token", accessToken).build())
                            .retrieve().bodyToMono(JsonNode.class)
                            .flatMap(jsonNode -> {
                                if (jsonNode.has("instagram_business_account") && jsonNode.get("instagram_business_account").has("id")) {
                                    return Mono.just(jsonNode.get("instagram_business_account").get("id").asText());
                                } else {
                                    return Mono.error(new RuntimeException("The first page ('" + response.data().getFirst().name() + "') is not connected to an Instagram Business Account."));
                                }
                            });
                });
    }

    public Mono<InstagramProfileDTO> getUserProfile(String instagramAccountId, String accessToken) {
        String fields = "id,username,name,biography,followers_count,profile_picture_url,media{id,media_url,caption,like_count,comments_count,timestamp,permalink}";

        // URL'ni xavfsiz usulda, UriComponentsBuilder yordamida quramiz
        URI uri = UriComponentsBuilder.fromUriString(userProfileUri)
                .path(instagramAccountId)
                .queryParam("fields", fields)
                .queryParam("access_token", accessToken)
                .build()
                .toUri();

        return webClient.get()
                .uri(uri) // Tayyor va xavfsiz URI'ni ishlatamiz
                .retrieve()
                .bodyToMono(InstagramProfileDTO.class);
    }
}