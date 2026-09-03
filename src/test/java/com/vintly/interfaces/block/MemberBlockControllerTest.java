package com.vintly.interfaces.block;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vintly.domain.block.dto.MemberBlockInfo;
import com.vintly.domain.block.service.MemberBlockService;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.service.CustomUserDetails;
import com.vintly.interfaces.member.MemberException;
import com.vintly.interfaces.presentation.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * 차단 API 의 요청 검증과 예외 매핑 테스트.
 *
 * 이 프로젝트는 예외를 HTTP 상태가 아니라 ApiResponse.code 로 돌려주므로 본문의 code 를 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class MemberBlockControllerTest {

    @Mock private MemberBlockService memberBlockService;
    @InjectMocks private MemberBlockController memberBlockController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    private static final Long MY_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberBlockController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        Member me = new Member(MY_ID, "me@test.com", "password", "myNick",
                "123456", "ROLE_USER", Use.Y, null, null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(me), null, List.of()));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private String body(Object memberId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("memberId", memberId);
        return objectMapper.writeValueAsString(map);
    }

    @Test
    @DisplayName("차단하면 성공으로 응답한다.")
    void blockRespondsSuccess() throws Exception {
        // given

        // when & then
        mockMvc.perform(post("/api/v1/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(2)))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200));

        verify(memberBlockService).block(MY_ID, 2L);
    }

    @Test
    @DisplayName("자기 자신을 차단하면 403으로 응답한다.")
    void selfBlockRespondsWith403() throws Exception {
        // given
        willThrow(new MemberBlockException.SelfBlockException())
                .given(memberBlockService).block(anyLong(), anyLong());

        // when & then
        mockMvc.perform(post("/api/v1/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(1)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("존재하지 않는 회원을 차단하면 404로 응답한다.")
    void unknownMemberRespondsWith404() throws Exception {
        // given
        willThrow(new MemberException.MemberNotFoundException())
                .given(memberBlockService).block(anyLong(), anyLong());

        // when & then
        mockMvc.perform(post("/api/v1/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(999)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("차단 대상 ID가 없으면 400으로 응답한다.")
    void missingMemberIdRespondsWith400() throws Exception {
        // given

        // when & then
        mockMvc.perform(post("/api/v1/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(null)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("차단을 해제하면 성공으로 응답한다.")
    void unblockRespondsSuccess() throws Exception {
        // given

        // when & then
        mockMvc.perform(delete("/api/v1/blocks/2"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200));

        verify(memberBlockService).unblock(MY_ID, 2L);
    }

    @Test
    @DisplayName("내 차단 목록은 상대 닉네임과 함께 반환된다.")
    void findMyBlocksReturnsList() throws Exception {
        // given
        given(memberBlockService.findMyBlocks(eq(MY_ID))).willReturn(List.of(
                new MemberBlockInfo.Blocked(2L, "blockedNick", LocalDateTime.of(2026, 9, 3, 12, 0))));

        // when & then
        mockMvc.perform(get("/api/v1/blocks"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].memberId").value(2))
                .andExpect(jsonPath("$.data[0].nickname").value("blockedNick"));
    }
}
