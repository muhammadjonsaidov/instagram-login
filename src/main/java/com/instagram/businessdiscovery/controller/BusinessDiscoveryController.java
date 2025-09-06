package com.instagram.businessdiscovery.controller;

import com.instagram.businessdiscovery.domain.User;
import com.instagram.businessdiscovery.dto.BusinessDiscoveryDto;
import com.instagram.businessdiscovery.service.BusinessDiscoveryService;
import com.instagram.businessdiscovery.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/business-discovery")
@RequiredArgsConstructor
@Slf4j
public class BusinessDiscoveryController {

    private final BusinessDiscoveryService businessDiscoveryService;
    private final UserService userService;

    @GetMapping
    public String businessDiscoveryPage(@RequestParam("userId") Long userId, Model model) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "User not found");
            return "error";
        }

        User user = userOpt.get();
        var searchStats = businessDiscoveryService.getSearchStatistics(user);
        
        model.addAttribute("user", user);
        model.addAttribute("searchStats", searchStats);
        
        return "business-discovery";
    }

    @PostMapping("/search")
    @ResponseBody
    public ResponseEntity<?> searchBusinessAccount(
            @RequestParam("userId") Long userId,
            @RequestParam("targetUsername") String targetUsername,
            @RequestParam(value = "includeMedia", defaultValue = "false") boolean includeMedia) {
        
        log.info("Business discovery search request - User ID: {}, Target: {}, Include Media: {}", 
                userId, targetUsername, includeMedia);
        
        try {
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            User user = userOpt.get();
            
            // Check for cached result first
            Optional<BusinessDiscoveryDto> cachedResult = businessDiscoveryService.getCachedResult(user, targetUsername);
            if (cachedResult.isPresent()) {
                log.debug("Returning cached result for target: {}", targetUsername);
                return ResponseEntity.ok(cachedResult.get());
            }
            
            // Perform new search
            BusinessDiscoveryDto result = businessDiscoveryService
                    .searchBusinessAccount(user, targetUsername, includeMedia)
                    .block();
            
            if (result != null) {
                log.info("Successfully completed business discovery for target: {}", targetUsername);
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body("No data found for the specified username");
            }
            
        } catch (Exception e) {
            log.error("Business discovery search failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Search failed: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public String searchHistory(@RequestParam("userId") Long userId, Model model) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "User not found");
            return "error";
        }

        User user = userOpt.get();
        var searchHistory = businessDiscoveryService.getSearchHistory(user);
        var searchStats = businessDiscoveryService.getSearchStatistics(user);
        
        model.addAttribute("user", user);
        model.addAttribute("searchHistory", searchHistory);
        model.addAttribute("searchStats", searchStats);
        
        return "search-history";
    }

    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<?> getSearchStats(@RequestParam("userId") Long userId) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();
        var searchStats = businessDiscoveryService.getSearchStatistics(user);
        
        return ResponseEntity.ok(searchStats);
    }
}