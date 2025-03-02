package com.vintly.member.model.req;

import com.vintly.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 회원가입 Req DTO
@Getter
@NoArgsConstructor
public class JoinReq {

    // nickname
    @NotBlank(message = "닉네임을 공백없이 입력해주세요.")
    @Pattern(regexp = "^[가-힣A-Za-z0-9_-]{2,10}$", message = "영어,한글 혹은 '-','_' 으로 1~10자로 입력해주세요.")
    private String nickname;

    // 이메일
    @Email
    @NotBlank(message = "이메일을 공백없이 입력해주세요")
    @Pattern(regexp = "^.{5,64}$", message = "64자 이하 이메일 형식으로 입력해주세요.")
    private String email;

    // 비밀번호
    @NotBlank(message = "비밀번호를 공백없이 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&~<>])[A-Za-z\\d@$!%*#?&~<>]{8,20}$",message = "비밀번호는 영어,숫자,특수문자를" +
            " 사용하여 8~20자로 입력해주세요.")
    private String password;

    // DTO to Entity
    public Member to(){
        return Member.builder()
                .nickname(nickname)
                .email(email)
                .password(password)
                .build();
    }

    // 비밀번호 암호화
    public void encPassword(String encodePassword){
        this.password = encodePassword;
    }
}
