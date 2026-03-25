package com.vintly.domain.board.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BoardInfo {

    public record BoardSummary(
            Long boardId,
            Long memberId,
            String authorNickname,
            String title,
            Integer viewCount,
            Long likeCount,
            Long commentCount,
            String thumbnailPath,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record BoardDetail(
            Long boardId,
            Long memberId,
            String authorNickname,
            String title,
            String content,
            int viewCount,
            long likeCount,
            boolean liked,
            List<BoardImgInfo> imgList,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record BoardImgInfo(
            Long boardImgId,
            String imgPath,
            int sortOrder
    ) {}
}
