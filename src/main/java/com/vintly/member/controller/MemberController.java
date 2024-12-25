package com.vintly.member.controller;

import com.vintly.common.ApiResponse;
import com.vintly.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService){
        this.memberService = memberService;
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(){
        return ResponseEntity.ok(ApiResponse.builder()
                .status(HttpStatus.OK)
                .msg("TEST")
                .data("").build());
    }

    /**
     * email 중복 체크
     * @param email
     * @return
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getChkEmail(@PathVariable("email") String email){
        return ResponseEntity.ok(ApiResponse.builder()
                .status(HttpStatus.OK)
                .msg("")
                .data(memberService.getChkEmail(email))
                .build());
    }
}
