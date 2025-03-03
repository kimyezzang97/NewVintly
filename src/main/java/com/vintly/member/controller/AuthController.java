package com.vintly.member.controller;

import com.vintly.common.config.ApiResponse;
import com.vintly.common.exception.StatusEnum;
import com.vintly.common.exception.memebr.NicknameValidException;
import com.vintly.member.model.req.JoinReq;
import com.vintly.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final MemberService memberService;

    public AuthController(MemberService memberService){
        this.memberService = memberService;
    }

    @GetMapping("/test")
    public String test(){
        return "test";
    }

    /**
     * nickname 중복 체크
     * @param nickname
     * @return status [OK or VALID_ERROR]
     */
    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<?> getChkNickname(@PathVariable("nickname") @NotBlank String nickname){
        if (!nickname.matches("^[가-힣A-Za-z0-9_-]{2,10}$")) throw new NicknameValidException();

        return ResponseEntity.ok(ApiResponse.builder()
                .status(StatusEnum.OK)
                .msg("사용 가능한 닉네임입니다.")
                .data(memberService.getChkNickname(nickname))
                .build());
    }

    /**
     * email 중복 체크
     * @param email
     * @return status [OK or VALID_ERROR]
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getChkEmail(@PathVariable("email") @NotBlank @Email @Size(max = 64) String email){
        return ResponseEntity.ok(ApiResponse.builder()
                .status(StatusEnum.OK)
                .msg("사용 가능한 이메일입니다.")
                .data(memberService.getChkEmail(email))
                .build());
    }

    /**
     * 회원가입
     * @param joinReq
     * @return
     */
    @PostMapping("/join")
    public ResponseEntity<?> createMember(@Valid @RequestBody JoinReq joinReq){
        memberService.createMember(joinReq);

        return ResponseEntity.ok(ApiResponse.builder()
                .status(StatusEnum.OK)
                .msg("회원가입에 성공하였습니다. 이메일 인증을 완료 해주세요.")
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
    public void verifyMember(@RequestParam String code, @RequestParam String email, HttpServletResponse res){
        Boolean isVerified = memberService.verifyEmail(code, email);
        try {
            if (isVerified){
                res.sendRedirect("/members/verify/join/success");
            } else {
                res.sendRedirect("/members/verify/join/fail");
            }
        } catch (IOException e){
            System.out.println(e.getMessage());
            System.out.println("\uD83D\uDD34 리다이렉트 중 오류 발생");
        }
    }

    /**
     * 토큰 재발급
     * @param request
     * @param response
     * @return accessToken, refreshToken
     */
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response){
        return memberService.reissue(request, response);
    }

}
