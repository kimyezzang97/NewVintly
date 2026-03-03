package com.vintly.interfaces.member;

import com.vintly.application.member.MemberFacade;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.service.MemberService;
import com.vintly.infra.config.swagger.api.SwaggerMemberApi;
import com.vintly.infra.util.SecurityUtil;
import com.vintly.interfaces.presentation.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@Validated
public class MemberController implements SwaggerMemberApi {

    private final MemberService memberService;
    private final MemberFacade memberFacade;

    @Autowired
    public MemberController(MemberService memberService, MemberFacade memberFacade) {
        this.memberService = memberService;
        this.memberFacade = memberFacade;
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ApiResponse<MemberResponse.Me> getMe() {
        String email = SecurityUtil.getCurrentEmail();
        Member member = memberService.findByEmail(email)
                .orElseThrow(MemberException.MemberNotFoundException::new);
        return new ApiResponse<>(true, 200, "", MemberResponse.Me.from(member));
    }

     // nickname 중복 체크
    @GetMapping("/nickname/{nickname}")
    public ApiResponse<?> getChkNickname(@PathVariable("nickname") @NotBlank String nickname){
        if (!nickname.matches("^[가-힣A-Za-z0-9_-]{2,10}$")) throw new MemberException.NicknameValidException();
        if (memberService.getChkNickname(nickname)) return new ApiResponse<>(false, 409, "이미 사용중인 닉네임입니다.", null);

        return new ApiResponse<>(true, 200, "사용 가능한 닉네임입니다.", null);
    }

    // email 중복 체크
    @GetMapping("/email/{email}")
    public ApiResponse<?> getChkEmail(@PathVariable("email") @NotBlank @Email @Size(max = 64) String email){
        if (memberService.getChkEmail(email)) return new ApiResponse<>(false, 409, "이미 사용중인 이메일입니다.", null);

        return new ApiResponse<>(true, 200, "사용 가능한 이메일입니다.", null);
    }

     // 회원가입
    @PostMapping("/join")
    public ApiResponse<?> createMember(@Valid @RequestBody MemberRequest.JoinMember join){
        memberFacade.joinMember(join);
        return new ApiResponse<>(true, 200, "회원가입에 성공하였습니다. 이메일 인증을 완료 해주세요.", null);
    }
}
