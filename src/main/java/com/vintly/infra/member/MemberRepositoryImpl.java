package com.vintly.infra.member;

import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.repo.MemberRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    public MemberRepositoryImpl(MemberJpaRepository memberJpaRepository) {
        this.memberJpaRepository = memberJpaRepository;
    }

    @Override
    public Boolean existsByEmail(String email) {
        return memberJpaRepository.existsByEmail(email);
    }

    @Override
    public Boolean existsByNickname(String nickname) {
        return memberJpaRepository.existsByNickname(nickname);
    }

    @Override
    public Integer deleteByCreatedAtBeforeAndUseYn(LocalDateTime today, Use use) {
        return memberJpaRepository.deleteByCreatedAtBeforeAndUseYn(today, use);
    }

    @Override
    public Optional<Member> findByEmailCodeAndEmail(String code, String email) {
        return memberJpaRepository.findByEmailCodeAndEmail(code, email);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email);
    }

    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }

    @Override
    public void delete(Member member) {
        memberJpaRepository.delete(member);
    }

    @Override
    public void deleteAll() {
        memberJpaRepository.deleteAll();
    }

    @Override
    public Member getReferenceById(Long memberId) {
        return memberJpaRepository.getReferenceById(memberId);
    }
}
