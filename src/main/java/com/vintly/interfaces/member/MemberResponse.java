package com.vintly.interfaces.member;

import com.vintly.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

public class MemberResponse {

    public record Me(
            @Schema(description = "회원 ID")
            Long memberId,

            @Schema(description = "닉네임")
            String nickname,

            @Schema(description = "이메일")
            String email,

            @Schema(description = "권한")
            String role
    ) {
        public static Me from(Member member) {
            return new Me(
                    member.getMemberId(),
                    member.getNickname(),
                    member.getEmail(),
                    member.getRole()
            );
        }
    }
}
