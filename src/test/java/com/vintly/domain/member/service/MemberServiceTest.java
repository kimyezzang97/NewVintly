package com.vintly.domain.member.service;

import com.vintly.domain.member.repo.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

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



}
