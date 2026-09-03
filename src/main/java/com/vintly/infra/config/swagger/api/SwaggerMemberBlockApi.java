package com.vintly.infra.config.swagger.api;

import com.vintly.domain.block.dto.MemberBlockInfo;
import com.vintly.interfaces.block.MemberBlockRequest;
import com.vintly.interfaces.presentation.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "MemberBlock", description = "사용자 차단 관련 API")
public interface SwaggerMemberBlockApi {

    @Operation(summary = "사용자 차단",
            description = "해당 회원의 글과 댓글이 내 화면에서 보이지 않게 합니다. 상대는 차단 여부를 알 수 없습니다. "
                    + "차단당한 회원은 내 게시글과 내 댓글에 댓글을 달 수 없습니다(403). "
                    + "이미 차단한 상대를 다시 차단해도 성공으로 응답합니다.",
            security = @SecurityRequirement(name = "access"))
    ApiResponse<?> block(@Valid @RequestBody MemberBlockRequest.Create req);

    @Operation(summary = "차단 해제",
            description = "차단을 해제합니다. 차단하지 않은 상대를 해제해도 성공으로 응답합니다.",
            security = @SecurityRequirement(name = "access"))
    ApiResponse<?> unblock(@PathVariable Long memberId);

    @Operation(summary = "내 차단 목록 조회",
            description = "내가 차단한 회원을 최신순으로 조회합니다. 차단 관리 화면에 쓰도록 닉네임을 함께 반환합니다.",
            security = @SecurityRequirement(name = "access"))
    ApiResponse<List<MemberBlockInfo.Blocked>> getMyBlocks();
}
