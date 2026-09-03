package com.vintly.interfaces.report;

import com.vintly.application.report.ReportFacade;
import com.vintly.domain.report.dto.ReportInfo;
import com.vintly.infra.config.swagger.api.SwaggerReportApi;
import com.vintly.infra.util.SecurityUtil;
import com.vintly.interfaces.presentation.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Validated
public class ReportController implements SwaggerReportApi {

    private final ReportFacade reportFacade;

    // 신고 접수 (게시글 / 게시판 댓글 / 매장 댓글을 한 엔드포인트로 받는다)
    @PostMapping
    public ApiResponse<ReportResponse.Create> createReport(@Valid @RequestBody ReportRequest.Create req) {
        Long reportId = reportFacade.report(SecurityUtil.getCurrentMemberId(),
                req.targetType(), req.targetId(), req.reason(), req.detail());

        return new ApiResponse<>(true, 200, "신고가 접수되었습니다.", new ReportResponse.Create(reportId));
    }

    // 내 신고 내역
    @GetMapping("/me")
    public ApiResponse<List<ReportInfo.My>> getMyReports() {
        return new ApiResponse<>(true, 200, "", reportFacade.findMyReports(SecurityUtil.getCurrentMemberId()));
    }
}
