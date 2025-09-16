package com.vintly.domain.mail.service;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.vintly.domain.mail.entity.Mail;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MailServiceTest {

    @Autowired
    private MailService mailService;

    // 가짜 mail test lib
    private static GreenMail greenMail;

    @BeforeAll // 전체 테스트 클래스 실행 전 단 한 번 실행되는 메서드
    static void setUpClass() {
        // 테스트용 SMTP 서버를 시작합니다.
        greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.start();
    }

    @BeforeEach // 각 테스트 실행 전에 실행되는 메서드
    void setUp() {
        // MailService의 fromAddress 필드는 @Value로 주입되므로
        // 테스트에서는 강제로 값을 설정합니다.
        ReflectionTestUtils.setField(mailService, "fromAddress", "noreply@example.com");
    }

    @AfterAll // 전체 테스트 클래스 실행 후 단 한 번 실행되는 메서드
    static void tearDownClass() {
        // 테스트가 끝나면 GreenMail 서버를 종료합니다
        greenMail.stop();
    }

    //@Test
    void testMailSend_success() throws Exception {
        // given - 테스트 메일 정보 준비
        Mail mail = new Mail("kingyezzang@naver.com", "회원가입 환영", "본문");
        Map<String, Object> values = Map.of("nickname", "테스터"); // 템플릿에 전달할 값

        // when - 메일 전송 메서드 호출
        mailService.mailSend(mail, values, "join"); // join.html 템플릿을 사용한다고 가정

        // then - GreenMail을 통해 메일이 정상적으로 수신되었는지 검증
        greenMail.waitForIncomingEmail(1); // 메일이 도착할 때까지 대기
        MimeMessage[] messages = greenMail.getReceivedMessages(); // 수신된 메일 목록 확인

        // 메일 수신 확인
        assertThat(messages).hasSize(1); // 메일이 1개 와야 함
        assertThat(messages[0].getSubject()).isEqualTo("회원가입 환영"); // 제목 확인
        assertThat(messages[0].getAllRecipients()[0].toString()).isEqualTo("kingyezzang@naver.com"); // 수신자 확인
    }
}