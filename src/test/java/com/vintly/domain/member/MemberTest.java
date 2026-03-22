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

    @Test
    @DisplayName("회원 탈퇴시 useYn이 N으로 변경되고 삭제 날짜가 생긴다.")
    void ifMemberLeavesUseStatusNAndCreateDeletedAt(){
        // given
        Member member = new Member(null, "test@test.com","password", "nickname",
                "123456", "ROLE_USER", Use.K, null);

        // when
        LocalDateTime deletedTime = LocalDateTime.now();
        member.leaveMember(deletedTime);

        // then
        Assertions.assertThat(member.getUseYn()).isEqualTo(Use.N);
        Assertions.assertThat(member.getDeletedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("닉네임 변경시 nickname이 새 값으로 변경된다.")
    void ifChangeNicknameThenNicknameUpdated(){
        // given
        Member member = new Member(null, "test@test.com", "password", "oldNick",
                "123456", "ROLE_USER", Use.Y, null);

        // when
        member.changeNickname("newNick");

        // then
        Assertions.assertThat(member.getNickname()).isEqualTo("newNick");
    }

    @Test
    @DisplayName("비밀번호 변경시 password가 새 값으로 변경된다.")
    void ifChangePasswordThenPasswordUpdated(){
        // given
        Member member = new Member(null, "test@test.com", "oldEncoded", "nickname",
                "123456", "ROLE_USER", Use.Y, null);

        // when
        member.changePassword("newEncoded");

        // then
        Assertions.assertThat(member.getPassword()).isEqualTo("newEncoded");
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