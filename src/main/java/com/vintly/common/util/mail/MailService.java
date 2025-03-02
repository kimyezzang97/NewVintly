package com.vintly.common.util.mail;

import com.vintly.common.exception.memebr.ConflictMemberException;
import com.vintly.common.util.mail.model.MailDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.util.HashMap;

@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Autowired
    public MailService(SpringTemplateEngine springTemplateEngine, JavaMailSender javaMailSender){
        this.templateEngine = springTemplateEngine;
        this.mailSender = javaMailSender;
    }

    // 메일 전송
    public void mailSend(MailDto mailDTO, HashMap<String, Object> values, String htmlName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            //메일 제목 설정
            helper.setSubject(mailDTO.getTitle());

            // 발신자 설정
            helper.setFrom(fromAddress);

            //수신자 설정
            helper.setTo(mailDTO.getAddress());

            //템플릿에 전달할 데이터 설정
            Context context = new Context();
            context.setVariables(values);

            //메일 내용 설정 : 템플릿 프로세스
            String html = templateEngine.process(htmlName, context);
            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e){
            System.out.println("\uD83D\uDCE7 메일 발송 실패! 회원가입 롤백됨");
            e.printStackTrace();
            throw new ConflictMemberException();
        }
    }
}
