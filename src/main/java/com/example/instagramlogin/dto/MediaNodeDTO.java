package com.example.instagramlogin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Data
public class MediaNodeDTO {
    private String id;
    private String media_url;
    private String caption;

    @JsonProperty("like_count")
    private Integer likeCount;

    // YANGI MAYDONLAR:
    @JsonProperty("comments_count")
    private Integer commentsCount;

    private String timestamp;

    private String permalink;

    public String getFormattedTimestamp() {
        if (this.timestamp == null || this.timestamp.isEmpty()) {
            return "";
        }
        try {
            // Instagram'dan keladigan "2025-08-21T19:15:30+0000" formatini parse qilamiz
            OffsetDateTime odt = OffsetDateTime.parse(this.timestamp);
            // Uni kerakli formatga o'giramiz ("21-Aug-2025 19:15")
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm", Locale.ENGLISH);
            return odt.format(formatter);
        } catch (Exception e) {
            // Agar formatlashda xato bo'lsa, asl matnni qaytaramiz
            return this.timestamp;
        }
    }

}