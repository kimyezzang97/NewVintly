package com.vintly.interfaces.auth;

import com.vintly.domain.auth.service.AuthService;
import com.vintly.domain.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/test")
    public String test(){
        return "test";
    }

    // 이메일 인증
    @GetMapping("/verify")
    public void verifyMember(@RequestParam String code, @RequestParam String email, HttpServletResponse res){
        Boolean isVerified = authService.verifyEmail(code, email);
        try {
            if (isVerified){
                res.sendRedirect("/members/verify/join/success");
            } else {
                res.sendRedirect("/members/verify/join/fail");
            }
        } catch (IOException e){
            log.warn("verifyMember {}", "\uD83D\uDD34 리다이렉트 중 오류 발생");
            log.warn("verifyMember e: {}", e.getMessage());
        }
    }

    // 토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response){
        return authService.reissue(request, response);
    }

}
