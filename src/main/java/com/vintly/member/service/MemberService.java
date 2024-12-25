package com.vintly.member.service;

import com.vintly.common.ApiResponse;
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

    // email 중복체크
    public Integer getChkEmail(String email){
        return memberRepository.countByEmail(email);
    }

}
