package com.instagram.businessdiscovery.controller;

import com.instagram.businessdiscovery.domain.User;
import com.instagram.businessdiscovery.service.BusinessDiscoveryService;
import com.instagram.businessdiscovery.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final UserService userService;
    private final BusinessDiscoveryService businessDiscoveryService;

    @GetMapping
    public String dashboard(@RequestParam("userId") Long userId, Model model) {
        log.debug("Loading dashboard for user ID: {}", userId);
        
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "User not found");
            return "error";
        }

        User user = userOpt.get();
        
        // Get user's search history
        var searchHistory = businessDiscoveryService.getSearchHistory(user);
        var searchStats = businessDiscoveryService.getSearchStatistics(user);
        var recentSearches = businessDiscoveryService.getRecentSearches(user);
        
        model.addAttribute("user", user);
        model.addAttribute("searchHistory", searchHistory);
        model.addAttribute("searchStats", searchStats);
        model.addAttribute("recentSearches", recentSearches);
        
        return "dashboard";
    }

    @GetMapping("/profile")
    public String profile(@RequestParam("userId") Long userId, Model model) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "User not found");
            return "error";
        }

        model.addAttribute("user", userOpt.get());
        return "profile";
    }
}