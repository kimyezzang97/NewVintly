package com.vintly.domain.member.service;

import com.vintly.domain.mail.entity.Mail;
import com.vintly.domain.member.Use;
import com.vintly.interfaces.member.MemberException;
import com.vintly.interfaces.member.MemberRequest;
import com.vintly.domain.mail.service.MailService;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    @Autowired
    public MemberService(MemberRepository memberRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.memberRepository = memberRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // email 중복 체크
    @Transactional(readOnly = true)
    public Boolean getChkEmail(String email){
        return memberRepository.existsByEmail(email);
    }

    // nickname 중복 체크
    @Transactional(readOnly = true)
    public Boolean getChkNickname(String nickname){
        return memberRepository.existsByNickname(nickname);
    }

    // 회원가입 return : emailCode
    public String createMember(MemberRequest.JoinMember join) {
        // 중복체크
        if(getChkEmail(join.email()) || getChkNickname(join.nickname())) throw new MemberException.ConflictMemberException();

        // 비밀번호 암호화
        String encodePassword = bCryptPasswordEncoder.encode(join.password());

        // email code random 6자리
        String emailCode = "" + ThreadLocalRandom.current().nextInt(100000, 1000000);

        // 회원의 권한
        String role = "ROLE_USER";

        // 회원정보 저장
        Member member = memberRepository.save(new Member(
                null, join.email(), encodePassword, join.nickname(), emailCode, role, Use.K, null)
                );

        return emailCode;
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    // 닉네임 변경
    @Transactional(rollbackFor = Exception.class)
    public void updateNickname(Member member, String nickname) {
        if (memberRepository.existsByNickname(nickname)) throw new MemberException.ConflictNicknameException();
        member.changeNickname(nickname);
    }

    // 비밀번호 변경
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Member member, String currentPassword, String newPassword) {
        if (!bCryptPasswordEncoder.matches(currentPassword, member.getPassword())) {
            throw new MemberException.PasswordNotMatchException();
        }
        member.changePassword(bCryptPasswordEncoder.encode(newPassword));
    }

    // 회원 탈퇴
    @Transactional(rollbackFor = Exception.class)
    public void withdrawMember(Member member) {
        member.leaveMember(java.time.LocalDateTime.now());
    }
}
