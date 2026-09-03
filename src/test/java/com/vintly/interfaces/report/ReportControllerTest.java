package com.vintly.interfaces.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vintly.application.report.ReportFacade;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.service.CustomUserDetails;
import com.vintly.domain.report.ReportReason;
import com.vintly.domain.report.ReportStatus;
import com.vintly.domain.report.ReportTargetType;
import com.vintly.domain.report.dto.ReportInfo;
import com.vintly.interfaces.presentation.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * 신고 API 의 요청 검증과 예외 매핑 테스트.
 *
 * 이 프로젝트는 예외를 HTTP 상태 코드가 아니라 {@code ApiResponse.code} 로 돌려주므로
 * (전역 핸들러에 {@code @ResponseStatus} 가 없다) 본문의 {@code code} 를 검증한다.
 *
 * 스프링 컨텍스트 없이 standalone MockMvc 로 컨트롤러와 전역 예외 핸들러만 물린다.
 * {@code SecurityUtil} 이 {@code SecurityContextHolder} 를 직접 읽으므로 인증 정보만 심어 준다.
 */
@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock private ReportFacade reportFacade;
    @InjectMocks private ReportController reportController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    private static final Long REPORTER_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        Member reporter = new Member(REPORTER_ID, "reporter@test.com", "password", "reporterNick",
                "123456", "ROLE_USER", Use.Y, null, null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(reporter), null, List.of()));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private String body(Object targetType, Object targetId, Object reason, Object detail) throws Exception {
        return objectMapper.writeValueAsString(mapOf(targetType, targetId, reason, detail));
    }

    private Map<String, Object> mapOf(Object targetType, Object targetId, Object reason, Object detail) {
        java.util.HashMap<String, Object> map = new java.util.HashMap<>();
        map.put("targetType", targetType);
        map.put("targetId", targetId);
        map.put("reason", reason);
        map.put("detail", detail);
        return map;
    }

    @Test
    @DisplayName("신고가 접수되면 접수 번호를 반환한다.")
    void createReportReturnsReportId() throws Exception {
        // given
        given(reportFacade.report(REPORTER_ID, ReportTargetType.BOARD, 100L, ReportReason.ABUSE, "욕설이 있습니다."))
                .willReturn(10L);

        // when & then
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("BOARD", 100, "ABUSE", "욕설이 있습니다.")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.reportId").value(10));
    }

    @Test
    @DisplayName("이미 신고한 대상이면 409로 응답한다.")
    void duplicateReportRespondsWith409() throws Exception {
        // given
        willThrow(new ReportException.DuplicateReportException())
                .given(reportFacade).report(anyLong(), any(), anyLong(), any(), any());

        // when & then
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("BOARD", 100, "ABUSE", null)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    @DisplayName("신고 대상이 없으면 404로 응답한다.")
    void missingTargetRespondsWith404() throws Exception {
        // given
        willThrow(new ReportException.ReportTargetNotFoundException())
                .given(reportFacade).report(anyLong(), any(), anyLong(), any(), any());

        // when & then
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("BOARD", 999, "ABUSE", null)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("본인이 작성한 콘텐츠를 신고하면 403으로 응답한다.")
    void selfReportRespondsWith403() throws Exception {
        // given
        willThrow(new ReportException.SelfReportException())
                .given(reportFacade).report(anyLong(), any(), anyLong(), any(), any());

        // when & then
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("BOARD", 100, "ABUSE", null)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("대상 종류가 없으면 400으로 응답한다.")
    void missingTargetTypeRespondsWith400() throws Exception {
        // given

        // when & then
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(null, 100, "ABUSE", null)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("신고 사유가 없으면 400으로 응답한다.")
    void missingReasonRespondsWith400() throws Exception {
        // given

        // when & then
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("BOARD", 100, null, null)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("내 신고 내역을 조회하면 접수한 신고 목록을 반환한다.")
    void findMyReportsReturnsList() throws Exception {
        // given
        given(reportFacade.findMyReports(eq(REPORTER_ID))).willReturn(List.of(
                new ReportInfo.My(10L, ReportTargetType.BOARD, 100L, ReportReason.ABUSE, "욕설이 있습니다.",
                        ReportStatus.PENDING, LocalDateTime.of(2026, 9, 3, 12, 0))));

        // when & then
        mockMvc.perform(get("/api/v1/reports/me"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].reportId").value(10))
                .andExpect(jsonPath("$.data[0].targetType").value("BOARD"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }
}
