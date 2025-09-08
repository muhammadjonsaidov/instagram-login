package com.instagram.businessdiscovery.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaEngagementDetails {
    private String id;
    private int likeCount;
    private int commentsCount;
    private int savedCount;
}
