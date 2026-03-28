package com.vintly.domain.member.service;

import com.vintly.domain.member.Use;
import com.vintly.interfaces.member.MemberException;
import com.vintly.interfaces.member.MemberRequest;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.domain.board.repo.BoardCommentRepository;
import com.vintly.domain.vintagecomment.repo.VintageCommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final VintageCommentRepository vintageCommentRepository;
    private final BoardCommentRepository boardCommentRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
                         VintageCommentRepository vintageCommentRepository,
                         BoardCommentRepository boardCommentRepository) {
        this.memberRepository = memberRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.vintageCommentRepository = vintageCommentRepository;
        this.boardCommentRepository = boardCommentRepository;
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
        // 이메일 중복 체크 (상태에 따라 분기)
        memberRepository.findByEmail(join.email()).ifPresent(existing -> {
            if (existing.getUseStatus() == Use.K) throw new MemberException.PendingEmailVerificationException();
            throw new MemberException.ConflictMemberException();
        });

        // 닉네임 중복 체크
        if(getChkNickname(join.nickname())) throw new MemberException.ConflictMemberException();

        // 비밀번호 암호화
        String encodePassword = bCryptPasswordEncoder.encode(join.password());

        // email code random 6자리
        String emailCode = "" + ThreadLocalRandom.current().nextInt(100000, 1000000);

        // 회원의 권한
        String role = "ROLE_USER";

        // 회원정보 저장
        Member member = memberRepository.save(new Member(
                null, join.email(), encodePassword, join.nickname(), emailCode, role, Use.K, null, null)
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

        if (member.getNicknameUpdatedAt() != null) {
            LocalDate allowedAt = member.getNicknameUpdatedAt().toLocalDate().plusDays(14);
            LocalDate today = LocalDate.now();
            if (today.isBefore(allowedAt)) {
                long remainingDays = Math.max(1, ChronoUnit.DAYS.between(today, allowedAt));
                throw new MemberException.NicknameChangeTooSoonException(remainingDays);
            }
        }

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
    public void withdrawMember(Member member, String password) {
        if (!bCryptPasswordEncoder.matches(password, member.getPassword())) {
            throw new MemberException.PasswordNotMatchException();
        }
        String deletedNickname = "del_" + member.getMemberId();
        vintageCommentRepository.anonymizeMemberInComments(member, deletedNickname);
        boardCommentRepository.anonymizeMemberInComments(member, deletedNickname);
        memberRepository.delete(member);
    }
}
