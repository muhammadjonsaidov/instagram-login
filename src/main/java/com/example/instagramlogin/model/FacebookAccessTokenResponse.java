package com.example.instagramlogin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

//@Data
public class FacebookAccessTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}