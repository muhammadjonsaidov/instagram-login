package com.instagram.businessdiscovery.controller;

import com.instagram.businessdiscovery.domain.User;
import com.instagram.businessdiscovery.service.InstagramApiService;
import com.instagram.businessdiscovery.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final InstagramApiService instagramApiService;
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
    public String home(Model model) {
        model.addAttribute("totalUsers", userService.getTotalUserCount());
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        String authUrl = UriComponentsBuilder.fromUriString(authorizationUri)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", scope)
                .queryParam("response_type", "code")
                .toUriString();
        
        log.debug("Redirecting to Instagram authorization URL");
        return "redirect:" + authUrl;
    }

    @GetMapping("/facebook/callback")
    public String handleCallback(@RequestParam("code") String code, Model model) {
        log.info("Received authorization callback with code");
        
        try {
            // Exchange code for access token and get user profile
            Mono<User> userMono = instagramApiService.getAccessToken(code)
                    .flatMap(accessToken -> 
                        instagramApiService.getInstagramBusinessAccountId(accessToken)
                                .flatMap(instagramAccountId -> 
                                    instagramApiService.getUserProfile(instagramAccountId, accessToken)
                                            .publishOn(Schedulers.boundedElastic())
                                            .map(profile -> userService.createOrUpdateUser(profile, accessToken))
                                )
                    );

            User user = userMono.block();
            
            if (user != null) {
                log.info("Successfully authenticated user: {}", user.getUsername());
                model.addAttribute("user", user);
                return "redirect:/dashboard?userId=" + user.getId();
            } else {
                model.addAttribute("error", "Failed to authenticate user");
                return "error";
            }
            
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage(), e);
            model.addAttribute("error", "Authentication failed: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/";
    }
}