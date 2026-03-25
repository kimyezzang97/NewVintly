package com.vintly.interfaces.board;

import com.vintly.domain.board.dto.BoardInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public class BoardResponse {

    public record BoardList(
            @Schema(description = "게시글 ID") Long boardId,
            @Schema(description = "작성자 ID") Long memberId,
            @Schema(description = "작성자 닉네임") String authorNickname,
            @Schema(description = "제목") String title,
            @Schema(description = "조회수") int viewCount,
            @Schema(description = "좋아요 수") long likeCount,
            @Schema(description = "댓글 수") long commentCount,
            @Schema(description = "대표 이미지 경로") String thumbnailPath,
            @Schema(description = "작성 시간") LocalDateTime createdAt,
            @Schema(description = "수정 시간") LocalDateTime updatedAt
    ) {
        public static BoardList from(BoardInfo.BoardSummary info) {
            return new BoardList(
                    info.boardId(),
                    info.memberId(),
                    info.authorNickname(),
                    info.title(),
                    info.viewCount(),
                    info.likeCount(),
                    info.commentCount(),
                    info.thumbnailPath(),
                    info.createdAt(),
                    info.updatedAt()
            );
        }
    }

    public record BoardDetail(
            @Schema(description = "게시글 ID") Long boardId,
            @Schema(description = "작성자 ID") Long memberId,
            @Schema(description = "작성자 닉네임") String authorNickname,
            @Schema(description = "제목") String title,
            @Schema(description = "본문 내용") String content,
            @Schema(description = "조회수") int viewCount,
            @Schema(description = "좋아요 수") long likeCount,
            @Schema(description = "사용자 좋아요 여부") boolean liked,
            @Schema(description = "이미지 목록") List<BoardImage> imgList,
            @Schema(description = "작성 시간") LocalDateTime createdAt,
            @Schema(description = "수정 시간") LocalDateTime updatedAt
    ) {
        public static BoardDetail from(BoardInfo.BoardDetail info) {
            return new BoardDetail(
                    info.boardId(),
                    info.memberId(),
                    info.authorNickname(),
                    info.title(),
                    info.content(),
                    info.viewCount(),
                    info.likeCount(),
                    info.liked(),
                    info.imgList().stream().map(BoardImage::from).toList(),
                    info.createdAt(),
                    info.updatedAt()
            );
        }
    }

    public record BoardImage(
            @Schema(description = "이미지 ID") Long boardImgId,
            @Schema(description = "이미지 경로") String imgPath,
            @Schema(description = "정렬 순서") int sortOrder
    ) {
        public static BoardImage from(BoardInfo.BoardImgInfo info) {
            return new BoardImage(info.boardImgId(), info.imgPath(), info.sortOrder());
        }
    }
}
