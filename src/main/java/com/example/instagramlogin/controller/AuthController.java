package com.example.instagramlogin.controller;

import com.example.instagramlogin.dto.UserResponseDTO;
import com.example.instagramlogin.model.User;
import com.example.instagramlogin.service.InstagramGraphApiService;
import com.example.instagramlogin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final InstagramGraphApiService instagramGraphApiService;
    private final UserService userService;

    @Value("${facebook.client-id}")
    private String clientId;
    @Value("${facebook.redirect-uri}")
    private String redirectUri;
    @Value("${facebook.scope}")
    private String scope;
    @Value("${facebook.api.authorization-uri}")
    private String authorizationUri;


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
        return instagramGraphApiService.getAccessToken(code)
                .flatMap(token -> instagramGraphApiService.getInstagramAccountId(token)
                        .flatMap(igAccountId -> instagramGraphApiService.getUserProfile(igAccountId, token))
                )
                .map(userProfile -> {
                    User savedUser = userService.processOAuthUser(userProfile);
                    // Entity'ni Response DTO'ga o'tkazamiz
                    UserResponseDTO userResponse = UserResponseDTO.builder()
                            .internalId(savedUser.getId())
                            .instagramId(savedUser.getInstagramId())
                            .username(savedUser.getUsername())
                            .fullName(savedUser.getFullName())
                            .profilePictureUrl(savedUser.getProfilePictureUrl())
                            .biography(savedUser.getBiography())
                            .media(userProfile.getMedia()) // Media ma'lumotlarini ham qo'shamiz
                            .build();

                    model.addAttribute("user", userResponse);

                    return "profile";
                })
                .onErrorResume(error -> {
                    model.addAttribute("error", error.getMessage());
                    return Mono.just("error");
                })
                .block();
    }
}