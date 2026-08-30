package com.vintly.interfaces.event.member;

import com.vintly.domain.event.MemberEvent;
import com.vintly.domain.mail.entity.Mail;
import com.vintly.domain.mail.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;

@Component
@Slf4j
public class MemberEventListener {

    private final MailService mailService;

    public MemberEventListener(MailService mailService) {
        this.mailService = mailService;
    }

    @Async // 비동기 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 예약 커밋 후 수행
    public void joinMailSend(MemberEvent memberEvent) {
        Mail mail = new Mail(memberEvent.getEmailAddress(), memberEvent.getEmailTitle(),
                memberEvent.getEmailMsg());

        HashMap<String, Object> emailValues = new HashMap<>();
        emailValues.put("nickname", memberEvent.getNickname());

        emailValues.put("url", memberEvent.getBaseUrl() +
                "/api/v1/auth/verify?code=" + memberEvent.getEmailCode() + "&email=" + memberEvent.getEmailAddress());

        mailService.mailSend(mail, emailValues,"join");
    }
}
