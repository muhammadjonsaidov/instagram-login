package com.example.instagramlogin.service;

import com.example.instagramlogin.dto.InstagramProfileDTO;
import com.example.instagramlogin.model.User;
import com.example.instagramlogin.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User processOAuthUser(InstagramProfileDTO profile) {
        User user = userRepository.findByInstagramId(profile.getId())
                .orElseGet(User::new);

        // Ma'lumotlarni Instagram'dan kelgan DTO'dan User entitisiga o'tkazamiz
        user.setInstagramId(profile.getId());
        user.setUsername(profile.getUsername());
        user.setFullName(profile.getName());
        user.setProfilePictureUrl(profile.getProfilePictureUrl());
        user.setBiography(profile.getBiography());

        return userRepository.save(user);
    }
}
