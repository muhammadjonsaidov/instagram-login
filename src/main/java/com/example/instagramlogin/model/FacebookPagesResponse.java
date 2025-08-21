package com.example.instagramlogin.model;

import java.util.List;

//@Data
public class FacebookPagesResponse {
    private List<FacebookPage> data;

    public List<FacebookPage> getData() {
        return data;
    }

    public void setData(List<FacebookPage> data) {
        this.data = data;
    }
}