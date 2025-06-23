package com.vintly.domain.member.integration;

import com.vintly.TestContainerConfig;
import com.vintly.application.member.MemberFacade;
import com.vintly.domain.mail.service.MailService;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.domain.member.service.MemberService;
import com.vintly.interfaces.event.member.MemberEventListener;
import com.vintly.interfaces.member.MemberRequest;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

// @Transactional async 테스트를 위해 주석 처리
@EnableAsync
@SpringBootTest
@Import({TestContainerConfig.class, MemberEventListener.class})
@ActiveProfiles("test")
public class MemberIntegrationTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private MemberEventListener memberEventListener;

    @MockitoBean
    private MailService mailService; // mock

    @Autowired
    private MemberFacade memberFacade;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원을 생성하면 이메일코드를 반환합니다.")
    void ifCreateMemberThenReturnEmailCode() {
        // given
        MemberRequest.JoinMember joinRequest = new MemberRequest.JoinMember(
                "kingyezzang@naver.com",
                "securePassword123!",
                "vintlyUser"
        );

        // when
        String emailCode = memberService.createMember(joinRequest);

        // then
        assertThat(emailCode).isNotNull();
        assertThat(emailCode).hasSize(6);

        Member saved = memberRepository.findByEmail(joinRequest.email()).orElseThrow();
        assertThat(saved.getNickname()).isEqualTo(joinRequest.nickname());
        assertThat(passwordEncoder.matches(joinRequest.password(), saved.getPassword())).isTrue();
        assertThat(saved.getUseYn()).isEqualTo(Use.K);
    }

    @Test
    @DisplayName("이메일이 존재하면 true, 존재하지 않으면 false를 반환합니다.")
    void shouldReturnTrueIfEmailExists() {
        // given
        String email = "kingyezzang@naver.com";
        Member member = new Member(null, email,"password", "nickname",
                "123456", "ROLE_USER", Use.K, null);
        memberRepository.save(member);

        // when
        Boolean exists = memberService.getChkEmail(email);
        Boolean notExists = memberService.getChkEmail("notexist@example.com");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("닉네임이 존재하면 true, 존재하지 않으면 false를 반환합니다.")
    void shouldReturnTrueIfNicknameExists() {
        // given
        String nickname = "nickname123";
        Member member = new Member(null, "test@1234.com","password", nickname,
                "123456", "ROLE_USER", Use.K, null);
        memberRepository.save(member);

        // when
        Boolean exists = memberService.getChkNickname(nickname);
        Boolean notExists = memberService.getChkNickname("nickname999");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("회원가입을 하면 멤버 생성 후 메일발송 event를 publish 합니다.")
    void ifCreateMemberThenPublishMailEvent(){
        // given
        MemberRequest.JoinMember join = new MemberRequest.JoinMember(
                "kingyezzang@naver.com", "secure123!", "eventUser"
        );

        // when
        memberFacade.joinMember(join);

        // then
        Awaitility.await() // 비동기 테스트 방법
                .atMost(Duration.ofSeconds(5)) // 최대 5초까지 대기
                .untilAsserted(() -> // 내부에 있는 JUnit 단언문(assert) 또는 Mockito 검증(verify) 이 예외 없이 통과될 때까지 반복 시도
                        verify(mailService).mailSend(any(), any(), anyString()) // 이 문장이 실행돼서 예외가 없으면 테스트 통과
                );
    }

}
