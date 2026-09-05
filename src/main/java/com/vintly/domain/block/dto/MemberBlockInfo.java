package com.vintly.domain.block.dto;

import com.vintly.domain.block.entity.MemberBlock;

import java.time.LocalDateTime;

public class MemberBlockInfo {

    // 내가 차단한 회원 한 명. 차단한 사람은 나 자신이므로 담지 않는다.
    public record Blocked(
            Long memberId,
            String nickname,
            LocalDateTime createdAt
    ) {
        public static Blocked from(MemberBlock block) {
            return new Blocked(
                    block.getBlocked().getMemberId(),
                    block.getBlocked().getNickname(),
                    block.getCreatedAt()
            );
        }
    }
}
