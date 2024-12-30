package com.vintly.common.util;

import com.vintly.member.model.constant.Use;
import com.vintly.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@EnableScheduling
@Component
public class MemberScheduled {

    private MemberRepository memberRepository;

    @Autowired
    public MemberScheduled(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
    }

    // 인증기간 지난 회원 삭제
    @Transactional
    @Scheduled(cron = "0 0 23 * * *") // 매일 23시 실행
    public void deleteExpiredId(){
        LocalDateTime oneDayBefore = LocalDateTime.now().minusDays(1);
        int i = memberRepository.deleteByCreateDateBeforeAndUseYn(oneDayBefore, Use.K);
    }
}
