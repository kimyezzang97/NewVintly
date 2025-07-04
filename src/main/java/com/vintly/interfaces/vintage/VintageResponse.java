package com.vintly.interfaces.vintage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class VintageResponse {

    // 빈티지 매장 전체 조회
    public record getVintageList(

            // vintage id
            Long vintageId,

            // 매장 이름
            String name,

            // 시/도 정보
            String state,

            // 구/시 정보
            String district,

            // 상세 주소
            String detailAddr,

            // 위도
            BigDecimal lat, //정확한 값을 위해 BigDecimal 사용

            // 경도
            BigDecimal lon,

            // 대표 이미지 경로
            String thumbnailPath
            ){}
}
