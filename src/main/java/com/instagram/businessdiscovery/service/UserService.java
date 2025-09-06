package com.instagram.businessdiscovery.service;

import com.instagram.businessdiscovery.domain.User;
import com.instagram.businessdiscovery.dto.InstagramProfileDto;
import com.instagram.businessdiscovery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createOrUpdateUser(InstagramProfileDto profileDto, String accessToken) {
        log.debug("Creating or updating user with Instagram ID: {}", profileDto.getId());

        Optional<User> existingUser = userRepository.findByInstagramId(profileDto.getId());

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.debug("Updating existing user: {}", user.getUsername());
        } else {
            user = new User();
            log.debug("Creating new user with Instagram ID: {}", profileDto.getId());
        }

        // Update user fields
        user.setInstagramId(profileDto.getId());
        user.setUsername(profileDto.getUsername());
        user.setFullName(profileDto.getName());
        user.setBiography(profileDto.getBiography());
        user.setProfilePictureUrl(profileDto.getProfilePictureUrl());
        user.setFollowersCount(profileDto.getFollowersCount());
        user.setFollowsCount(profileDto.getFollowsCount());
        user.setMediaCount(profileDto.getMediaCount());
        /*user.setAccountType(profileDto.getAccountType());*/
        user.setAccessToken(accessToken);

        // Determine if it's a business account
        /*user.setIsBusinessAccount("BUSINESS".equalsIgnoreCase(profileDto.getAccountType()) ||
                                 "CREATOR".equalsIgnoreCase(profileDto.getAccountType()));*/

        User savedUser = userRepository.save(user);
        log.info("Successfully saved user: {} (ID: {})", savedUser.getUsername(), savedUser.getId());

        return savedUser;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByInstagramId(String instagramId) {
        return userRepository.findByInstagramId(instagramId);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /*@Transactional(readOnly = true)
    public List<User> findAllBusinessAccounts() {
        return userRepository.findBusinessAccountsOrderByFollowers();
    }*/

    @Transactional(readOnly = true)
    public List<User> findUsersWithMinFollowers(Integer minFollowers) {
        return userRepository.findUsersWithMinFollowers(minFollowers);
    }

    @Transactional(readOnly = true)
    public boolean existsByInstagramId(String instagramId) {
        return userRepository.existsByInstagramId(instagramId);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public User updateAccessToken(Long userId, String newAccessToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setAccessToken(newAccessToken);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        return userRepository.count();
    }
}