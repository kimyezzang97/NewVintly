package com.vintly.interfaces.member;

import com.vintly.domain.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MemberRequest {

    public record JoinMember (
        // 닉네임
       @NotBlank(message = "닉네임을 공백없이 입력해주세요.")
       @Pattern(regexp = "^[가-힣A-Za-z0-9_-]{2,10}$", message = "닉네임은 영어,한글 혹은 '-','_' 으로 2~10자로 입력해주세요.")
       String nickname,

       // 이메일
       @Email(message = "이메일을 형식에 맞게 입력해주세요.")
       @NotBlank(message = "이메일을 공백없이 입력해주세요")
       @Pattern(regexp = "^.{5,64}$", message = "64자 이하 이메일 형식으로 입력해주세요.")
       String email,

       // 비밀번호
       @NotBlank(message = "비밀번호를 공백없이 입력해주세요.")
       @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&~<>])[A-Za-z\\d@$!%*#?&~<>]{8,20}$",message = "비밀번호는 영어,숫자,특수문자를" +
            " 사용하여 8~20자로 입력해주세요.")
       String password
    ){
        @Override
        public String toString() {
            return "JoinMember[nickname=%s, email=%s]".formatted(nickname, email);
        }
    }

}
