package com.example.instagramlogin.controller;

import com.example.instagramlogin.service.InstagramGraphApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Controller
public class AuthController {

    private final InstagramGraphApiService instagramGraphApiService;

    @Value("${facebook.client-id}")
    private String clientId;
    @Value("${facebook.redirect-uri}")
    private String redirectUri;
    @Value("${facebook.scope}")
    private String scope;
    @Value("${facebook.api.authorization-uri}")
    private String authorizationUri;

    public AuthController(InstagramGraphApiService instagramGraphApiService) {
        this.instagramGraphApiService = instagramGraphApiService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login/instagram-business")
    public String loginWithInstagram() {
        String url = UriComponentsBuilder.fromUriString(authorizationUri)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", scope)
                .toUriString();
        return "redirect:" + url;
    }

    @GetMapping("/facebook/callback")
    public String facebookCallback(@RequestParam("code") String code, Model model) {
        Mono<String> accessTokenMono = instagramGraphApiService.getAccessToken(code).cache();

        return accessTokenMono
                .flatMap(token -> instagramGraphApiService.getInstagramAccountId(token)
                        .flatMap(igAccountId -> instagramGraphApiService.getUserProfile(igAccountId, token))
                )
                .map(userProfile -> {
                    model.addAttribute("profile", userProfile);
                    return "profile";
                })
                .onErrorResume(error -> {
                    model.addAttribute("error", error.getMessage());
                    return Mono.just("error");
                })
                .block();
    }
}