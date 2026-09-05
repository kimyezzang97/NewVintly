package com.vintly.domain.block;

import com.vintly.domain.block.entity.MemberBlock;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberBlockTest {

    private Member member(Long memberId, String nickname) {
        return new Member(memberId, memberId + "@test.com", "password", nickname,
                "123456", "ROLE_USER", Use.Y, null, null);
    }

    // 자기 자신 차단 차단은 서비스에서 막는다. 엔티티에서 IllegalArgumentException을 던지면
    // 전역 핸들러가 400으로 매핑하는데, 자기 차단은 신고의 SelfReportException과 맞춰 403이다.
    @Test
    @DisplayName("차단을 생성하면 차단한 사람과 차단당한 사람이 그대로 담긴다.")
    void createKeepsBlockerAndBlocked() {
        // given
        Member blocker = member(1L, "blockerNick");
        Member blocked = member(2L, "blockedNick");

        // when
        MemberBlock block = MemberBlock.create(blocker, blocked);

        // then
        Assertions.assertThat(block.getBlocker()).isSameAs(blocker);
        Assertions.assertThat(block.getBlocked()).isSameAs(blocked);
    }
}
