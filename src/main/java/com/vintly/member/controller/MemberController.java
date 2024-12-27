package com.vintly.member.controller;

import com.vintly.common.ApiResponse;
import com.vintly.member.model.req.JoinReq;
import com.vintly.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * nickname 중복 체크
     * @param nickname
     * @return
     */
    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<?> getChkNickname(@PathVariable("nickname") String nickname){
        return ResponseEntity.ok(ApiResponse.builder()
                .status(HttpStatus.OK)
                .msg("")
                .data(memberService.getChkNickname(nickname))
                .build());
    }

    @PostMapping("/join")
    public void createMember(@Valid @RequestBody JoinReq joinReq){
        memberService.createMember(joinReq);
    }
}
