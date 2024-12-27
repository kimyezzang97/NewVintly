package com.vintly.member.service;

import com.vintly.common.ApiResponse;
import com.vintly.entity.Member;
import com.vintly.member.model.req.JoinReq;
import com.vintly.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
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

        }

        // 회원정보 저장
        memberRepository.save(joinReq.to());
    }
}
