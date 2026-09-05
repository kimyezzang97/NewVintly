package com.vintly.interfaces.block;

import com.vintly.domain.block.dto.MemberBlockInfo;
import com.vintly.domain.block.service.MemberBlockService;
import com.vintly.infra.config.swagger.api.SwaggerMemberBlockApi;
import com.vintly.infra.util.SecurityUtil;
import com.vintly.interfaces.presentation.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blocks")
@RequiredArgsConstructor
@Validated
public class MemberBlockController implements SwaggerMemberBlockApi {

    private final MemberBlockService memberBlockService;

    // 차단 (이미 차단한 상대여도 성공으로 응답한다)
    @PostMapping
    public ApiResponse<?> block(@Valid @RequestBody MemberBlockRequest.Create req) {
        memberBlockService.block(SecurityUtil.getCurrentMemberId(), req.memberId());
        return new ApiResponse<>(true, 200, "차단했습니다.", null);
    }

    // 차단 해제 (차단하지 않은 상대여도 성공으로 응답한다)
    @DeleteMapping("/{memberId}")
    public ApiResponse<?> unblock(@PathVariable Long memberId) {
        memberBlockService.unblock(SecurityUtil.getCurrentMemberId(), memberId);
        return new ApiResponse<>(true, 200, "차단을 해제했습니다.", null);
    }

    // 내 차단 목록
    @GetMapping
    public ApiResponse<List<MemberBlockInfo.Blocked>> getMyBlocks() {
        return new ApiResponse<>(true, 200, "", memberBlockService.findMyBlocks(SecurityUtil.getCurrentMemberId()));
    }
}
