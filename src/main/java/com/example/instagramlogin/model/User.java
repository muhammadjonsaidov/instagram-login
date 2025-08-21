package com.example.instagramlogin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor // JPA uchun bo'sh konstruktor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String instagramId;

    @Column(nullable = false)
    private String username;

    private String fullName;

    @Column(length = 512)
    private String profilePictureUrl;

    @Column(columnDefinition = "TEXT")
    private String biography;
}