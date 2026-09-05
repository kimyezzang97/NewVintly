package com.vintly.interfaces.block;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class MemberBlockRequest {

    @Schema(name = "MemberBlockCreate")
    public record Create(
            @Schema(description = "차단할 회원 ID", example = "2")
            @NotNull(message = "차단할 회원을 선택해주세요.")
            @Positive(message = "회원 ID가 올바르지 않습니다.")
            Long memberId
    ) {}
}
