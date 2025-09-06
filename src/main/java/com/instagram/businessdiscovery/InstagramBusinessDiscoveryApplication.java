package com.instagram.businessdiscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class InstagramBusinessDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstagramBusinessDiscoveryApplication.class, args);
    }
}