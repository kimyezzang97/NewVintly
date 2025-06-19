package com.vintly.domain.member.service;

import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        // Mockito.verify(memberRepository).existsByEmail(email); // 1번 호출 되었는지 확인 방법
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

        // 중복 확인시 false 리턴하게 세팅
        Mockito.when(memberRepository.existsByEmail(join.email())).thenReturn(false);
        Mockito.when(memberRepository.existsByNickname(join.nickname())).thenReturn(false);

        // 비밀번호 인코더가 특정값 리턴하게 세팅
        Mockito.when(bCryptPasswordEncoder.encode(join.password())).thenReturn("encoded");

        // 저장시 Member가 리턴된다.
        Member savedMember = new Member(
                1L,
                "test@example.com",
                "encoded",
                "nickname123",
                "123123",
                "ROLE_USER",
                Use.K,
                null
        );
        Mockito.when(memberRepository.save(ArgumentMatchers.any(Member.class))).thenReturn(savedMember);

        // when
        String emailCode = memberService.createMember(join);

        // then
        assertNotNull(emailCode);
        assertEquals(6, emailCode.length());

        // 호출 확인
        Mockito.verify(memberRepository).existsByEmail(join.email());
        Mockito.verify(memberRepository).existsByNickname(join.nickname());
        Mockito.verify(bCryptPasswordEncoder).encode(join.password());
        Mockito.verify(memberRepository).save(ArgumentMatchers.any(Member.class));
    }

}
