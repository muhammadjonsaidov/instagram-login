package com.example.instagramlogin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

//@Data
public class FacebookPage {
    private String id;
    private String name;
    @JsonProperty("instagram_business_account")
    private InstagramBusinessAccount instagramBusinessAccount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InstagramBusinessAccount getInstagramBusinessAccount() {
        return instagramBusinessAccount;
    }

    public void setInstagramBusinessAccount(InstagramBusinessAccount instagramBusinessAccount) {
        this.instagramBusinessAccount = instagramBusinessAccount;
    }
}