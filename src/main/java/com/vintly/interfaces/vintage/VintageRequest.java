package com.vintly.interfaces.vintage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public class VintageRequest {

        // 빈티지 매장 등록
        public record CreateVintage(

            // 매장 이름
            @Schema(description = "빈티지 매장 이름")
            @NotBlank(message = "매장 이름을 입력해주세요.")
            String name,

            @Schema(description = "시/도 정보")
            @NotBlank(message = "시/도 정보를 입력해주세요.")
            String state,

            @Schema(description = "구/시 정보")
            @NotBlank(message = "구/시 정보를 입력해주세요.")
            String district,

            @Schema(description = "상세 주소")
            @NotBlank(message = "상세 주소를 입력해주세요.")
            String detailAddr,

            @Schema(description = "위도")
            @NotNull(message = "위도를 입력해주세요.") // NotBlank 는 String 에만
            @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
            @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
            BigDecimal lat, //정확한 값을 위해 BigDecimal 사용

            @Schema(description = "경도")
            @NotNull(message = "경도를 입력해주세요.")
            @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
            @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
            BigDecimal lon,

            @NotNull(message = "이미지는 필수입니다.")
            @Size(min = 1, max = 10, message = "이미지는 1장 이상 10장 이하만 업로드 가능합니다.")
            List<MultipartFile> images
        ){}

        // 빈티지 매장 수정
        public record UpdateVintage(

                // 매장 이름
                @Schema(description = "빈티지 매장 이름")
                String name,

                @Schema(description = "시/도 정보")
                String state,

                @Schema(description = "구/시 정보")
                String district,

                @Schema(description = "상세 주소")
                String detailAddr,

                @Schema(description = "위도")
                @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
                @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
                BigDecimal lat, //정확한 값을 위해 BigDecimal 사용

                @Schema(description = "경도")
                @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
                @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
                BigDecimal lon,

                @Size(min = 1, max = 10, message = "이미지는 1장 이상 10장 이하만 업로드 가능합니다.")
                List<MultipartFile> images
        ){}


}
