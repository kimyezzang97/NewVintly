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
                "123456", "ROLE_USER", Use.K, null, null);

        //when
        member.enableMember();

        //then
        Assertions.assertThat(member.getUseYn()).isEqualTo(Use.Y);
    }

    @Test
    @DisplayName("닉네임 변경시 nickname이 새 값으로 변경된다.")
    void ifChangeNicknameThenNicknameUpdated(){
        // given
        Member member = new Member(null, "test@test.com", "password", "oldNick",
                "123456", "ROLE_USER", Use.Y, null, null);

        // when
        member.changeNickname("newNick");

        // then
        Assertions.assertThat(member.getNickname()).isEqualTo("newNick");
    }

    @Test
    @DisplayName("닉네임 변경시 nicknameUpdatedAt이 현재 시간으로 설정된다.")
    void ifChangeNicknameThenNicknameUpdatedAtIsSet(){
        // given
        Member member = new Member(null, "test@test.com", "password", "oldNick",
                "123456", "ROLE_USER", Use.Y, null, null);
        LocalDateTime before = LocalDateTime.now();

        // when
        member.changeNickname("newNick");

        // then
        Assertions.assertThat(member.getNicknameUpdatedAt()).isNotNull();
        Assertions.assertThat(member.getNicknameUpdatedAt()).isAfterOrEqualTo(before);
        Assertions.assertThat(member.getNicknameUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("비밀번호 변경시 password가 새 값으로 변경된다.")
    void ifChangePasswordThenPasswordUpdated(){
        // given
        Member member = new Member(null, "test@test.com", "oldEncoded", "nickname",
                "123456", "ROLE_USER", Use.Y, null, null);

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
                "123456", "ROLE_USER", Use.K, null, null);

        // when
        LocalDateTime deletedTime = LocalDateTime.now();
        member.vanMember(deletedTime);

        // then
        Assertions.assertThat(member.getUseYn()).isEqualTo(Use.X);
        Assertions.assertThat(member.getDeletedAt()).isBefore(LocalDateTime.now());
    }
}
