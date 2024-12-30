package com.vintly.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/members/verify")
public class MemberVerifyController {

    /**
     * 이메일 인증 성공 페이지
     * @return
     */
    @GetMapping("/success")
    public String emailConfirmationSuccess() {
        return "verify-success"; // `resources/templates/verify-success.html`로 매핑
    }

    /**
     * 이메일 인증 실패 페이지
     * @return
     */
    @GetMapping("/fail")
    public String emailConfirmationError() {
        return "verify-fail";
    }
}
