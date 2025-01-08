package com.vintly.member.service;

import com.vintly.common.ApiResponse;
import com.vintly.common.exception.memebr.ConflictMemberException;
import com.vintly.common.util.mail.MailService;
import com.vintly.common.util.mail.model.MailDto;
import com.vintly.entity.Member;
import com.vintly.member.model.req.JoinReq;
import com.vintly.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.mapping.Join;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;

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
    public Integer getChkEmail(String email){
        return memberRepository.countByEmail(email);
    }

    // nickname 중복 체크
    public Integer getChkNickname(String nickname){return memberRepository.countByNickname(nickname);}

    // 회원가입
    public void createMember(JoinReq joinReq){
        // 중복체크
        if(getChkEmail(joinReq.getEmail()) > 0 || getChkNickname(joinReq.getNickname()) > 0){
            throw new ConflictMemberException();
        }

        // 비밀번호 암호화
        String encodePassword = bCryptPasswordEncoder.encode(joinReq.getPassword());
        joinReq.encPassword(encodePassword);

        // 회원정보 저장
        String code = memberRepository.save(joinReq.to()).getEmailCode();

        // 인증메일 발송
        try {
            mailSend(joinReq, code);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 회원가입 인증 메일 발송
    public void mailSend(JoinReq joinReq, String code) throws MessagingException, IOException {
        MailDto mailDTO = MailDto.builder()
                .address(joinReq.getEmail())
                .title("회원가입")
                .message("회원가입 메시지")
                .build();

        HashMap<String, String> emailValues = new HashMap<>();
        emailValues.put("nickname", joinReq.getNickname());

        emailValues.put("url", "http://" + serverAddress + ":" + serverPort +
                "/api/v1/members/verify?code=" + code + "&email=" + joinReq.getEmail());

        mailService.mailSend(mailDTO, emailValues,"join");
    }

    // 계정 인증
    public Integer verifyEmail(String code, String email, HttpServletResponse res){
        Integer isVerified = memberRepository.countByEmailCodeAndEmail(code, email);

        if(isVerified == 1){
            Member member = memberRepository.findByEmailCodeAndEmail(code, email);
            member.enableMember();
            memberRepository.save(member);
        }

        return isVerified;
    }
}
