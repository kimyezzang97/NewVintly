package com.vintly.infra.config.swagger.api;

import com.vintly.domain.report.dto.ReportInfo;
import com.vintly.interfaces.presentation.ApiResponse;
import com.vintly.interfaces.report.ReportRequest;
import com.vintly.interfaces.report.ReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Report", description = "신고 관련 API")
public interface SwaggerReportApi {

    @Operation(summary = "신고 접수",
            description = "게시글, 게시판 댓글, 매장 댓글을 신고합니다. targetType 으로 대상 종류를 구분합니다. "
                    + "본인이 작성한 콘텐츠는 신고할 수 없고(403), 같은 대상을 두 번 신고할 수 없습니다(409). "
                    + "접수된 신고는 PENDING 상태로 저장되며 관리자가 검토합니다.",
            security = @SecurityRequirement(name = "access"))
    ApiResponse<ReportResponse.Create> createReport(@Valid @RequestBody ReportRequest.Create req);

    @Operation(summary = "내 신고 내역 조회",
            description = "현재 로그인한 회원이 접수한 신고를 최신순으로 조회합니다.",
            security = @SecurityRequirement(name = "access"))
    ApiResponse<List<ReportInfo.My>> getMyReports();
}
