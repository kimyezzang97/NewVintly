package com.vintly.infra.config.swagger.api;

import com.vintly.interfaces.member.MemberRequest;
import com.vintly.interfaces.presentation.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Member", description = "회원 관련 API")
public interface SwaggerMemberApi {

    @Operation(summary = "닉네임 중복 체크", description = "해당 닉네임의 중복을 체크합니다.")
    ApiResponse<?> getChkNickname(@PathVariable("nickname") @NotBlank String nickname);

    @Operation(summary = "이메일 중복 체크", description = "해당 이메일의 중복을 체크합니다.")
    ApiResponse<?> getChkEmail(@PathVariable("email") @NotBlank @Email @Size(max = 64) String email);

    @Operation(summary = "회원가입", description = "해당 정보로 회원가입합니다.")
    ApiResponse<?> createMember(@Valid @RequestBody MemberRequest.JoinMember join);
}
