package com.vintly.interfaces.report;

import com.vintly.domain.report.ReportReason;
import com.vintly.domain.report.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ReportRequest {

    @Schema(name = "ReportCreate")
    public record Create(
            @Schema(description = "신고 대상 종류", example = "BOARD")
            @NotNull(message = "신고 대상 종류를 선택해주세요.")
            ReportTargetType targetType,

            @Schema(description = "신고 대상 ID", example = "100")
            @NotNull(message = "신고 대상을 선택해주세요.")
            @Positive(message = "신고 대상 ID가 올바르지 않습니다.")
            Long targetId,

            @Schema(description = "신고 사유", example = "ABUSE")
            @NotNull(message = "신고 사유를 선택해주세요.")
            ReportReason reason,

            @Schema(description = "상세 사유 (선택)")
            @Size(max = 500, message = "상세 사유는 500자를 넘을 수 없습니다.")
            String detail
    ) {}
}
