package com.vintly.domain.member.service;

import com.vintly.domain.mail.entity.Mail;
import com.vintly.domain.member.Use;
import com.vintly.interfaces.member.MemberException;
import com.vintly.interfaces.member.MemberRequest;
import com.vintly.domain.mail.service.MailService;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class MemberService {

    @Value("${company.address}")
    private String serverAddress;

    @Value("${company.port}")
    private String serverPort;

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MailService mailService;


    @Autowired
    public MemberService(MemberRepository memberRepository, BCryptPasswordEncoder bCryptPasswordEncoder, MailService mailService) {
        this.memberRepository = memberRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.mailService = mailService;
    }

    // email 중복 체크
    @Transactional(readOnly = true)
    public Boolean getChkEmail(String email){
        return memberRepository.existsByEmail(email);
    }

    // nickname 중복 체크
    @Transactional(readOnly = true)
    public Boolean getChkNickname(String nickname){
        return memberRepository.existsByNickname(nickname);
    }

    // 회원가입
    @Transactional(rollbackFor = Exception.class)
    public void createMember(MemberRequest.JoinMember join) {
        // 중복체크
        if(getChkEmail(join.email()) || getChkNickname(join.nickname())) throw new MemberException.ConflictMemberException();

        // 비밀번호 암호화
        String encodePassword = bCryptPasswordEncoder.encode(join.password());

        // email code random 6자리
        String emailCode = "" + ThreadLocalRandom.current().nextInt(100000, 1000000);

        // 회원의 권한
        String role = "ROLE_USER";

        // 회원정보 저장
        Member member = memberRepository.save(new Member(
                null, join.email(), encodePassword, join.nickname(), emailCode, role, Use.K, null)
                );

        // 인증메일 발송
        mailSend(member);
    }

    // 회원가입 인증 메일 발송
    public void mailSend(Member member) {
        Mail mail = new Mail(member.getEmail(), "회원가입", "회원가입 메시지");

        HashMap<String, Object> emailValues = new HashMap<>();
        emailValues.put("nickname", member.getNickname());

        emailValues.put("url", "http://" + serverAddress + ":" + serverPort +
                "/api/v1/auth/verify?code=" + member.getEmailCode() + "&email=" + member.getEmail());

        mailService.mailSend(mail, emailValues,"join");
    }

    // 계정 인증
    @Transactional(rollbackFor = Exception.class)
    public Boolean verifyEmail(String code, String email){
        Optional<Member> optionalMember = memberRepository.findByEmailCodeAndEmail(code, email);

        if(optionalMember.isEmpty()){
            log.info("이메일 인증 실패 {}", email);
            return false;
        }

        Member member = optionalMember.get();
        member.enableMember();

        return true;
    }

}
