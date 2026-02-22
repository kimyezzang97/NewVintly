package com.vintly.interfaces.vintage;

import com.vintly.domain.vintage.dto.VintageInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VintageResponse {

    // 빈티지 매장 전체 조회
    public record VintageList(

            @Schema(description = "vintage id")
            Long vintageId,

            @Schema(description = "매장 이름")
            String name,

            @Schema(description = "시/도 정보")
            String state,

            @Schema(description = "구/시 정보")
            String district,

            @Schema(description = "상세 주소")
            String detailAddr,

            @Schema(description = "위도")
            BigDecimal lat, //정확한 값을 위해 BigDecimal 사용

            @Schema(description = "경도")
            BigDecimal lon,

            @Schema(description = "대표 이미지 경로")
            String thumbnailPath
            ){}

    // 빈티지 매장 상세 조회
    public record Vintage(

            @Schema(description = "vintage id")
            Long vintageId,

            @Schema(description = "매장 이름")
            String name,

            @Schema(description = "시/도 정보")
            String state,

            @Schema(description = "구/시 정보")
            String district,

            @Schema(description = "상세 주소")
            String detailAddr,

            @Schema(description = "위도")
            BigDecimal lat, //정확한 값을 위해 BigDecimal 사용

            @Schema(description = "경도")
            BigDecimal lon,

            @Schema(description = "이미지 리스트")
            List<Image> imgList,

            @Schema(description = "좋아요 수")
            int likeCount,

            @Schema(description = "사용자의 좋아요 여부")
            boolean liked,

            @Schema(description = "댓글 목록")
            List<Comment> comments
    ){
        public static Vintage from(VintageInfo.VintageDetail info) {
            return new Vintage(
                    info.vintageId(),
                    info.name(),
                    info.state(),
                    info.district(),
                    info.detailAddr(),
                    info.lat(),
                    info.lon(),
                    info.imgList().stream()
                            .map(Image::from)
                            .toList(),
                    info.likeCount(),
                    info.liked(),
                    info.comments().stream()
                            .map(Comment::from)
                            .toList()
            );
        }
    }

    public record Image(
            @Schema(description = "이미지 ID")
            Long vintageImgId,

            @Schema(description = "이미지 URL")
            String imgPath
    ){
        public static Image from(VintageInfo.Image info) {
            return new Image(
                    info.vintageImgId(),
                    info.imgPath()
            );
        }
    }

    public record Comment(
            @Schema(description = "댓글 ID")
            Long commentId,

            @Schema(description = "상위 댓글 ID (0이면 최상위 댓글)")
            Long parentCommentId,

            @Schema(description = "댓글 작성자 id")
            Long memberId,

            @Schema(description = "댓글 작성자 닉네임")
            String nickname,

            @Schema(description = "댓글 내용")
            String content,

            @Schema(description = "댓글 작성일")
            LocalDateTime createdAt
    ) {
        public static Comment from(VintageInfo.Comment info) {
            return new Comment(
                    info.commentId(),
                    info.parentCommentId(),
                    info.memberId(),
                    info.nickname(),
                    info.content(),
                    info.createdAt()
            );
        }
    }


}
