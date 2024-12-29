package com.vintly.member.service;

import com.vintly.common.ApiResponse;
import com.vintly.common.exception.memebr.ConflictMemberException;
import com.vintly.entity.Member;
import com.vintly.member.model.req.JoinReq;
import com.vintly.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private MemberRepository memberRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository, BCryptPasswordEncoder bCryptPasswordEncoder){
        this.memberRepository = memberRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
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
        memberRepository.save(joinReq.to());
    }
}
