package com.vintly.domain.member;

import com.vintly.domain.member.entity.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class MemberTest {

    @DisplayName("회원 활성화시 useYn이 Y로 변경된다.")
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

//    @Test
//    @DisplayName("회원 탈퇴시 useYn이 N으로 변경되고 삭제 날짜가 생긴다.")
    void ifMemberLeavesUseStatusNAndCreateDeletedAt(){
        // given
        Member member = new Member(null, "test@test.com","password", "nickname",
                "123456", "ROLE_USER", Use.K, null);

        // when
        LocalDateTime deletedTime = LocalDateTime.now();
        member.leaveMember(deletedTime);

        // then
        Assertions.assertThat(member.getUseYn()).isEqualTo(Use.N);
        Assertions.assertThat(member.getDeletedAt()).isBefore(LocalDateTime.now());
    }

    @Test
    @DisplayName("회원 추방시 useYn이 X로 변경되고 삭제 날짜가 생긴다.")
    void ifMemberVanUseStatusNAndCreateDeletedAt(){
        // given
        Member member = new Member(null, "test@test.com","password", "nickname",
                "123456", "ROLE_USER", Use.K, null);

        // when
        LocalDateTime deletedTime = LocalDateTime.now();
        member.vanMember(deletedTime);

        // then
        Assertions.assertThat(member.getUseYn()).isEqualTo(Use.X);
        Assertions.assertThat(member.getDeletedAt()).isBefore(LocalDateTime.now());
    }
}