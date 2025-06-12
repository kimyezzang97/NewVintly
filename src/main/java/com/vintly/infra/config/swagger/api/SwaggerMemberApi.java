package com.vintly.infra.config.swagger.api;

import com.vintly.interfaces.member.MemberRequest;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    com.vintly.interfaces.presentation.ApiResponse<?> getChkNickname(@PathVariable("nickname") @NotBlank String nickname);

    @Operation(summary = "이메일 중복 체크", description = "해당 이메일의 중복을 체크합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "이메일 사용 가능 응답",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "사용 가능 이메일",
                                    summary = "사용 가능 이메일 응답",
                                    value = """
                        {
                          "success": true,
                          "code": 200,
                          "message": "사용 가능한 이메일입니다.",
                          "data": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 사용중인 이메일",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "사용 중",
                                    summary = "이미 사용 중인 이메일",
                                    value = """
                        {
                          "success": false,
                          "code": 409,
                          "message": "이미 사용중인 이메일입니다.",
                          "data": null
                        }
                        """
                            )
                    )
            )
    })
    com.vintly.interfaces.presentation.ApiResponse<?> getChkEmail(@Parameter(description = "중복 체크할 이메일", example = "vintage@example.com") @PathVariable("email") @NotBlank @Email @Size(max = 64) String email);

    @Operation(summary = "회원가입", description = "해당 정보로 회원가입합니다.")
    com.vintly.interfaces.presentation.ApiResponse<?> createMember(@Valid @RequestBody MemberRequest.JoinMember join);
}
