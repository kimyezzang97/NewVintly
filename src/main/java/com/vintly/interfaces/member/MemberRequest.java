package com.vintly.interfaces.member;

import com.vintly.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MemberRequest {

    /**
     * 비밀번호 규칙: 영문 + 숫자 + 특수문자를 모두 포함한 8~20자.
     *
     * <p>예전에는 특수문자를 11개(@$!%*#?&~&lt;&gt;)만 허용해서 ^ ( ) - _ + = . , 같은
     * 흔한 문자가 막혔다. 비밀번호는 BCrypt로 해싱해 저장하므로 문자를 좁힐 이유가 없어,
     * 공백을 뺀 출력 가능한 ASCII 전체를 허용한다.
     *
     * <p>비ASCII(한글·이모지)는 제외한다. BCrypt는 72바이트에서 잘리는데
     * 멀티바이트 문자가 섞이면 사용자가 모르는 사이 뒷부분이 무시될 수 있다.
     *
     * <p>가입과 비밀번호 변경 두 곳에서 쓰므로 반드시 이 상수를 통해 참조할 것.
     */
    private static final String PASSWORD_REGEX =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d])[\\x21-\\x7E]{8,20}$";

    private static final String PASSWORD_MESSAGE =
            "비밀번호는 영어, 숫자, 특수문자를 모두 포함해 8~20자로 입력해주세요. (공백과 한글은 사용할 수 없습니다)";

    public record ChangeNickname(
            @Schema(description = "새 닉네임", example = "새닉네임")
            @NotBlank(message = "닉네임을 공백없이 입력해주세요.")
            @Pattern(regexp = "^(?!del_)[가-힣A-Za-z0-9_-]{2,10}$", message = "닉네임은 영어,한글 혹은 '-','_' 으로 2~10자로 입력해주세요.")
            String nickname
    ) {}

    public record ChangePassword(
            @Schema(description = "현재 비밀번호", example = "current@123")
            @NotBlank(message = "현재 비밀번호를 입력해주세요.")
            String currentPassword,

            @Schema(description = "새 비밀번호", example = "newP@ssword1")
            @NotBlank(message = "새 비밀번호를 공백없이 입력해주세요.")
            @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MESSAGE)
            String newPassword
    ) {}

    public record WithdrawMember(
            @Schema(description = "현재 비밀번호", example = "current@123")
            @NotBlank(message = "비밀번호를 입력해주세요.")
            String password
    ) {}

    public record JoinMember (
        // 닉네임
       @Schema(description = "닉네임", example = "김예짱")
       @NotBlank(message = "닉네임을 공백없이 입력해주세요.")
       @Pattern(regexp = "^(?!del_)[가-힣A-Za-z0-9_-]{2,10}$", message = "닉네임은 영어,한글 혹은 '-','_' 으로 2~10자로 입력해주세요.")
       String nickname,

       // 이메일
       @Schema(description = "이메일", example = "test@naver.com")
       @Email(message = "이메일을 형식에 맞게 입력해주세요.")
       @NotBlank(message = "이메일을 공백없이 입력해주세요")
       @Pattern(regexp = "^.{5,64}$", message = "64자 이하 이메일 형식으로 입력해주세요.")
       String email,

       // 비밀번호
       @Schema(description = "비밀번호", example = "p@ssword1")
       @NotBlank(message = "비밀번호를 공백없이 입력해주세요.")
       @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MESSAGE)
       String password
    ){
        @Override
        public String toString() {
            return "MemberRequest-JoinMember[nickname=%s, email=%s]".formatted(nickname, email);
        }
    }

}
