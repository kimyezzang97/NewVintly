package com.vintly.interfaces.youtube;

import com.vintly.domain.youtube.dto.YoutubeLinkInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class YoutubeLinkResponse {

    public record YoutubeLinkList(
            @Schema(description = "유튜브 링크 ID") Long youtubeLinkId,
            @Schema(description = "유튜브 URL") String url,
            @Schema(description = "제목") String title,
            @Schema(description = "설명") String description,
            @Schema(description = "광고 여부") boolean isAd,
            @Schema(description = "생성 시간") LocalDateTime createdAt,
            @Schema(description = "수정 시간") LocalDateTime updatedAt
    ) {
        public static YoutubeLinkList from(YoutubeLinkInfo.YoutubeLinkSummary info) {
            return new YoutubeLinkList(
                    info.youtubeLinkId(),
                    info.url(),
                    info.title(),
                    info.description(),
                    info.isAd(),
                    info.createdAt(),
                    info.updatedAt()
            );
        }
    }
}
