package com.vintly.domain.member.service;

import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.interfaces.member.MemberException;
import com.vintly.interfaces.member.MemberRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("해당 이메일이 존재할 경우 true를 반환하고 존재하지 않으면 false를 반환한다.")
    void shouldReturnTrueIfEmailExists() {
        // given
        String email = "test@example.com";
        Mockito.when(memberRepository.existsByEmail(email)).thenReturn(true);

        // when
        Boolean existResult = memberService.getChkEmail(email);
        Boolean notExistResult = memberService.getChkEmail("test2@example.com");

        // then
        assertThat(existResult).isTrue();
        assertThat(notExistResult).isFalse();
    }

    @Test
    @DisplayName("해당 닉네임이 존재할 경우 true를 반환하고 존재하지 않으면 false를 반환한다.")
    void shouldReturnTrueIfNicknameExists() {
        // given
        String nickname = "nickname123";
        Mockito.when(memberRepository.existsByNickname(nickname)).thenReturn(true);

        // when
        Boolean existResult = memberService.getChkNickname(nickname);
        Boolean notExistResult = memberService.getChkNickname("nickname1234");

        // then
        assertThat(existResult).isTrue();
        assertThat(notExistResult).isFalse();

        Mockito.verify(memberRepository).existsByNickname(nickname);
    }

    @Test
    @DisplayName("전달받은 Request로 Member를 생성한다.")
    void createMemberShouldSaveMemberAndReturnEmailCode() {
        // given
        MemberRequest.JoinMember join = new MemberRequest.JoinMember(
                "nickname123",
                "test@example.com",
                "secure123@"
        );

        Mockito.when(memberRepository.existsByEmail(join.email())).thenReturn(false);
        Mockito.when(memberRepository.existsByNickname(join.nickname())).thenReturn(false);
        Mockito.when(bCryptPasswordEncoder.encode(join.password())).thenReturn("encoded");

        Member savedMember = new Member(
                1L,
                "test@example.com",
                "encoded",
                "nickname123",
                "123123",
                "ROLE_USER",
                Use.K,
                null,
                null
        );
        Mockito.when(memberRepository.save(ArgumentMatchers.any(Member.class))).thenReturn(savedMember);

        // when
        String emailCode = memberService.createMember(join);

        // then
        assertNotNull(emailCode);
        assertEquals(6, emailCode.length());

        Mockito.verify(memberRepository).existsByEmail(join.email());
        Mockito.verify(memberRepository).existsByNickname(join.nickname());
        Mockito.verify(bCryptPasswordEncoder).encode(join.password());
        Mockito.verify(memberRepository).save(ArgumentMatchers.any(Member.class));
    }

    @Test
    @DisplayName("이메일로 회원을 조회하면 회원 정보를 반환한다")
    void shouldReturnMemberWhenEmailExists() {
        // Arrange
        String email = "test@example.com";
        Member member = new Member(1L, email, "password", "닉네임",
                "code", "ROLE_USER", Use.Y, null, null);
        Mockito.when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        // Act
        Optional<Member> result = memberService.findByEmail(email);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getMemberId()).isEqualTo(1L);
        assertThat(result.get().getEmail()).isEqualTo(email);
        assertThat(result.get().getNickname()).isEqualTo("닉네임");
        assertThat(result.get().getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회하면 빈 Optional을 반환한다")
    void shouldReturnEmptyWhenEmailNotExists() {
        // Arrange
        String email = "unknown@example.com";
        Mockito.when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<Member> result = memberService.findByEmail(email);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("닉네임 중복이 없고 최초 변경이면 닉네임이 변경된다.")
    void shouldUpdateNicknameWhenNotDuplicated() {
        // Arrange
        Member member = new Member(1L, "test@example.com", "encoded", "oldNick",
                "code", "ROLE_USER", Use.Y, null, null);
        Mockito.when(memberRepository.existsByNickname("newNick")).thenReturn(false);

        // Act
        memberService.updateNickname(member, "newNick");

        // Assert
        assertThat(member.getNickname()).isEqualTo("newNick");
    }

    @Test
    @DisplayName("닉네임 변경 후 14일이 지나면 재변경이 가능하다.")
    void shouldUpdateNicknameWhenAfter14Days() {
        // Arrange
        LocalDateTime fifteenDaysAgo = LocalDateTime.now().minusDays(15);
        Member member = new Member(1L, "test@example.com", "encoded", "oldNick",
                "code", "ROLE_USER", Use.Y, null, fifteenDaysAgo);
        Mockito.when(memberRepository.existsByNickname("newNick")).thenReturn(false);

        // Act
        memberService.updateNickname(member, "newNick");

        // Assert
        assertThat(member.getNickname()).isEqualTo("newNick");
    }

    @Test
    @DisplayName("닉네임 변경 후 14일 이내에 재변경 시 NicknameChangeTooSoonException이 발생하고 남은 기간을 알려준다.")
    void shouldThrowWhenNicknameChangedWithin14Days() {
        // Arrange
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        Member member = new Member(1L, "test@example.com", "encoded", "oldNick",
                "code", "ROLE_USER", Use.Y, null, threeDaysAgo);
        Mockito.when(memberRepository.existsByNickname("newNick")).thenReturn(false);

        // Act & Assert
        MemberException.NicknameChangeTooSoonException ex = assertThrows(
                MemberException.NicknameChangeTooSoonException.class,
                () -> memberService.updateNickname(member, "newNick")
        );
        assertThat(ex.getRemainingDays()).isEqualTo(11);
        assertThat(ex.getMessage()).contains("11");
    }

    @Test
    @DisplayName("닉네임이 중복이면 ConflictNicknameException이 발생한다.")
    void shouldThrowWhenNicknameDuplicated() {
        // Arrange
        Member member = new Member(1L, "test@example.com", "encoded", "oldNick",
                "code", "ROLE_USER", Use.Y, null, null);
        Mockito.when(memberRepository.existsByNickname("dupNick")).thenReturn(true);

        // Act & Assert
        assertThrows(
                MemberException.ConflictNicknameException.class,
                () -> memberService.updateNickname(member, "dupNick")
        );
    }

    @Test
    @DisplayName("현재 비밀번호가 일치하면 새 비밀번호로 변경된다.")
    void shouldUpdatePasswordWhenCurrentPasswordMatches() {
        // Arrange
        Member member = new Member(1L, "test@example.com", "oldEncoded", "nickname",
                "code", "ROLE_USER", Use.Y, null, null);
        Mockito.when(bCryptPasswordEncoder.matches("currentPw", "oldEncoded")).thenReturn(true);
        Mockito.when(bCryptPasswordEncoder.encode("newPw")).thenReturn("newEncoded");

        // Act
        memberService.updatePassword(member, "currentPw", "newPw");

        // Assert
        assertThat(member.getPassword()).isEqualTo("newEncoded");
    }

    @Test
    @DisplayName("현재 비밀번호가 틀리면 PasswordNotMatchException이 발생한다.")
    void shouldThrowWhenCurrentPasswordNotMatch() {
        // Arrange
        Member member = new Member(1L, "test@example.com", "oldEncoded", "nickname",
                "code", "ROLE_USER", Use.Y, null, null);
        Mockito.when(bCryptPasswordEncoder.matches("wrongPw", "oldEncoded")).thenReturn(false);

        // Act & Assert
        assertThrows(
                MemberException.PasswordNotMatchException.class,
                () -> memberService.updatePassword(member, "wrongPw", "newPw")
        );
    }

    @Test
    @DisplayName("회원 탈퇴시 DB에서 즉시 삭제된다.")
    void shouldWithdrawMember() {
        // Arrange
        Member member = new Member(1L, "test@example.com", "encoded", "nickname",
                "code", "ROLE_USER", Use.Y, null, null);

        // Act
        memberService.withdrawMember(member);

        // Assert
        Mockito.verify(memberRepository).delete(member);
    }
}
