package com.vintly.infra.config.swagger.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Auth", description = "인증 관련 API")
public interface SwaggerAuthApi {

    @Operation(summary = "이메일 인증", description = "회원가입 후 이메일로 발송된 코드로 이메일 인증을 완료합니다.")
    void verifyMember(
            @Parameter(description = "인증 코드", example = "123456") @RequestParam String code,
            @Parameter(description = "인증할 이메일", example = "user@example.com") @RequestParam String email,
            HttpServletResponse res
    );

    @Operation(summary = "토큰 재발급", description = "Refresh 토큰으로 Access 토큰을 재발급합니다.")
    ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response);
}
