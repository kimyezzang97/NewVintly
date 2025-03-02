package com.vintly.member.service;

import com.vintly.common.exception.memebr.ConflictMemberException;
import com.vintly.common.util.mail.MailService;
import com.vintly.common.util.mail.model.MailDto;
import com.vintly.entity.Member;
import com.vintly.member.model.req.JoinReq;
import com.vintly.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

@Service
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
    public Boolean getChkNickname(String nickname){return memberRepository.existsByNickname(nickname);}

    // 회원가입
    @Transactional(rollbackFor = Exception.class)
    public void createMember(JoinReq joinReq) {
        // 중복체크
        if(getChkEmail(joinReq.getEmail()) || getChkNickname(joinReq.getNickname())) throw new ConflictMemberException();

        // 비밀번호 암호화
        String encodePassword = bCryptPasswordEncoder.encode(joinReq.getPassword());
        joinReq.encPassword(encodePassword);

        // 회원정보 저장
        String code = memberRepository.save(joinReq.to()).getEmailCode();

        // 인증메일 발송
        mailSend(joinReq, code);
    }

    // 회원가입 인증 메일 발송
    public void mailSend(JoinReq joinReq, String code) {
        MailDto mailDTO = MailDto.builder()
                .address(joinReq.getEmail())
                .title("회원가입")
                .message("회원가입 메시지")
                .build();

        HashMap<String, Object> emailValues = new HashMap<>();
        emailValues.put("nickname", joinReq.getNickname());

        emailValues.put("url", "http://" + serverAddress + ":" + serverPort +
                "/api/v1/auth/verify?code=" + code + "&email=" + joinReq.getEmail());

        mailService.mailSend(mailDTO, emailValues,"join");
    }

    // 계정 인증
    @Transactional(rollbackFor = Exception.class)
    public Boolean verifyEmail(String code, String email){
        Optional<Member> optionalMember = memberRepository.findByEmailCodeAndEmail(code, email);

        if(optionalMember.isEmpty()){
            System.out.println("이메일 인증 실패");
            return false;
        }

        Member member = optionalMember.get();
        member.enableMember();
        memberRepository.save(member);

        return true;
    }
}
