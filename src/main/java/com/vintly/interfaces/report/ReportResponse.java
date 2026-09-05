package com.vintly.interfaces.report;

import io.swagger.v3.oas.annotations.media.Schema;

public class ReportResponse {

    @Schema(name = "ReportCreateResult")
    public record Create(
            @Schema(description = "접수된 신고 ID", example = "10")
            Long reportId
    ) {}
}
