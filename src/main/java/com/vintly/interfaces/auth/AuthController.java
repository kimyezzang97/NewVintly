package com.vintly.interfaces.auth;

import com.vintly.domain.auth.service.AuthService;
import com.vintly.domain.member.service.CustomUserDetails;
import com.vintly.infra.config.swagger.api.SwaggerAuthApi;
import com.vintly.infra.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@Slf4j
public class AuthController implements SwaggerAuthApi {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/test")
    public String test(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//        String email = userDetails.getUsername();

        String email = SecurityUtil.getCurrentEmail();
        System.out.println("email : " + email);
        return "test";
    }

    @GetMapping("/test2")
    public String test2(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        System.out.println("email : " + email);
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
