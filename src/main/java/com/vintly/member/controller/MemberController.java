package com.vintly.member.controller;

import com.vintly.common.ApiResponse;
import com.vintly.common.exception.StatusEnum;
import com.vintly.common.exception.memebr.NicknameValidException;
import com.vintly.member.model.req.JoinReq;
import com.vintly.member.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/members")
@Validated
public class MemberController {

    private MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService){
        this.memberService = memberService;
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(){
        return ResponseEntity.ok(ApiResponse.builder()
                .status(StatusEnum.OK)
                .msg("TEST")
                .data("").build());
    }

    /**
     * email 중복 체크
     * @param email
     * @return
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getChkEmail(@PathVariable("email") @NotBlank @Email @Size(max = 64) String email){
        return ResponseEntity.ok(ApiResponse.builder()
                .status(StatusEnum.OK)
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
    public ResponseEntity<?> getChkNickname(@PathVariable("nickname") @NotBlank String nickname){
        if (!nickname.matches("^[가-힣A-Za-z0-9_-]{1,10}$")) {
            throw new NicknameValidException();
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .status(StatusEnum.OK)
                .msg("")
                .data(memberService.getChkNickname(nickname))
                .build());
    }

    /**
     * 회원가입
     * @param joinReq
     * @return
     */
    @PostMapping("")
    public ResponseEntity<?> createMember(@Valid @RequestBody JoinReq joinReq){
        memberService.createMember(joinReq);

        return ResponseEntity.ok(ApiResponse.builder()
                .status(StatusEnum.OK)
                .msg("")
                .data("")
                .build());
    }

    /**
     * 이메일 인증
     * @param code
     * @param email
     * @param res
     * @throws IOException
     */
    @GetMapping("/verify")
    public void verifyMember(@RequestParam String code, @RequestParam String email, HttpServletResponse res) throws IOException {
        Integer isVerified = memberService.verifyEmail(code, email, res);
        System.out.println(isVerified);
        if (isVerified == 1){
            res.sendRedirect("/members/verify/success");
        } else {
            res.sendRedirect("/members/verify/fail");
        }
    }
}
