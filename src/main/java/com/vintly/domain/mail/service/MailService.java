package com.vintly.domain.mail.service;

import com.vintly.domain.mail.entity.Mail;
import com.vintly.interfaces.member.MemberException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${mail.from:${spring.mail.username}}")
    private String fromAddress;

    @Autowired
    public MailService(SpringTemplateEngine springTemplateEngine, JavaMailSender javaMailSender){
        this.templateEngine = springTemplateEngine;
        this.mailSender = javaMailSender;
    }

    // 메일 전송
    public void mailSend(Mail mail, Map<String, Object> values, String htmlName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            //메일 제목 설정
            helper.setSubject(mail.getTitle());

            // 발신자 설정
            helper.setFrom(fromAddress);

            //수신자 설정
            helper.setTo(mail.getAddress());

            //템플릿에 전달할 데이터 설정
            Context context = new Context();
            context.setVariables(values);

            //메일 내용 설정 : 템플릿 프로세스
            String html = templateEngine.process(htmlName, context);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("메일 발송 성공 - To:{}, Title:{}, Template:{}",
                    mail.getAddress(), mail.getTitle(), htmlName);
        } catch (Exception e){
            // 오류시 더욱 상세하게 로그 남깁니다.
            log.error("메일 발송 실패 - To:{}, Title:{}, Template:{}, Exception:{}, Stacktrace:{}}",
                    mail.getAddress(),
                    mail.getTitle(),
                    htmlName,
                    e.toString(),
                    ExceptionUtils.getStackTrace(e)); // ExceptionUtils는 Apache Commons Lang의 유틸
        }
    }
}
