package com.vintly.application.member;

import com.vintly.domain.event.MemberEvent;
import com.vintly.domain.event.MemberEventPublisher;
import com.vintly.domain.member.service.MemberService;
import com.vintly.interfaces.member.MemberRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class MemberFacade {

    private MemberService memberService;
    private MemberEventPublisher memberEventPublisher;

    @Value("${company.base-url}")
    private String baseUrl;

    public MemberFacade(MemberService memberService, MemberEventPublisher memberEventPublisher) {
        this.memberService = memberService;
        this.memberEventPublisher = memberEventPublisher;
    }

    // 회원 가입
    @Transactional(rollbackFor = Exception.class)
    public void joinMember(MemberRequest.JoinMember join){
        // member 생성
        String emailCode = memberService.createMember(join);

        // 메일 발송 event 전달
        memberEventPublisher.publish(
                new MemberEvent(join.email(), join.nickname(), "회원가입", "회원가입 메시지", emailCode, baseUrl)
        );
    }
}
