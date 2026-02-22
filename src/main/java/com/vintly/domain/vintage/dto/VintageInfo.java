package com.vintly.domain.vintage.dto;

import com.vintly.interfaces.vintage.VintageResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VintageInfo {

    public record Vintage(
            // vintage id
            Long vintageId,

            // 매장이름
            String name,

            // 시/도 정보
            String state,

            // 구/시 정보
            String district,

            // 상세 주소
            String detailAddr,

            // 위도
            BigDecimal lat,

            // 경도
            BigDecimal lon,

            // 대표 이미지 경로
            String thumbnailPath
    ){}

    public record VintageDetail(
            // vintage id
            Long vintageId,

            // 매장이름
            String name,

            // 시/도 정보
            String state,

            // 구/시 정보
            String district,

            // 상세 주소
            String detailAddr,

            // 위도
            BigDecimal lat,

            // 경도
            BigDecimal lon,

            // 이미지 리스트
            List<Image> imgList,

            // 좋아요 수
            int likeCount,

            // 사용자의 좋아요 여부
            boolean liked,

            // 댓글 목록
            List<Comment> comments
    ) {}

    public record Comment(
            // 댓글 ID
            Long commentId,

            // 상위 댓글 ID (0이면 최상위 댓글)
            Long parentCommentId,

            // 댓글 작성자 id
            Long memberId,

            // 댓글 작성자 닉네임
            String nickname,

            // 댓글 내용
            String content,

            // 댓글 작성일
            LocalDateTime createdAt
    ) {}

    public record Image(
            // 이미지 ID
            Long vintageImgId,

            // 이미지 URL
            String imgPath
    ){}
}
