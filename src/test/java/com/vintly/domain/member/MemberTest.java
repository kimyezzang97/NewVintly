package com.vintly.domain.member;

import com.vintly.domain.member.entity.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    @DisplayName("회원 활성화시 useYn 이 Y로 변경된다.")
    @Test
    void ifEnableMemberUseYnStatusToY() {
        //given
        Member member = new Member(null, "test@test.com","password", "nickname",
                "123456", "ROLE_USER", Use.K, null);

        //when
        member.enableMember();

        //then
        Assertions.assertThat(member.getUseYn()).isEqualTo(Use.Y);
    }
}