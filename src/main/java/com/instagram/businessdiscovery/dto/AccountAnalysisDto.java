package com.instagram.businessdiscovery.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class AccountAnalysisDto {
    private int postCount;
    private long totalLikes;
    private long totalComments;
    private long totalSaves;
    private double averageEngagementRate;
    private long followersAtTimeOfAnalysis;
    private LocalDate startDate;
    private LocalDate endDate;
}