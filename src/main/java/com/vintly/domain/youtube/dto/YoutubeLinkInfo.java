package com.vintly.domain.youtube.dto;

import java.time.LocalDateTime;

public class YoutubeLinkInfo {

    public record YoutubeLinkSummary(
            Long youtubeLinkId,
            String url,
            String title,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
