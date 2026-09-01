package com.vintly.domain.vintagelike.service;

import com.vintly.interfaces.member.MemberException;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintage.repo.VintageRepository;
import com.vintly.domain.vintagelike.entity.VintageLike;
import com.vintly.domain.vintagelike.repo.VintageLikeRepository;
import com.vintly.infra.util.SecurityUtil;
import com.vintly.interfaces.vintagelike.VintageLikeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VintageLikeService {

    private final VintageLikeRepository vintageLikeRepository;
    private final VintageRepository vintageRepository;
    private final MemberRepository memberRepository;

    // 해당 빈티지 매장의 좋아요 수 카운트
    @Transactional(readOnly = true)
    public int countLikesByVintageId(Long vintageId) {
        return vintageLikeRepository.countLikesByVintageId(vintageId);
    }

    // 특정 유저가 해당 매장에 좋아요를 눌렀는지 여부 확인
    @Transactional(readOnly = true)
    public boolean existsLikeByVintageIdAndMemberId(Long vintageId, Long memberId){
        return vintageLikeRepository.existsLikeByVintageIdAndMemberId(vintageId, memberId);
    }

    @Transactional
    public void deleteAllLikesByVintage(Vintage vintage){
        vintageLikeRepository.deleteAllLikesByVintage(vintage);
    }

    @Transactional
    public VintageLikeResponse.VintageLike like(Long vintageId) {

        Long memberId = SecurityUtil.getCurrentMemberId();

        // 이미 있으면 멱등 처리
        if (!vintageLikeRepository.existsLikeByVintageIdAndMemberId(vintageId, memberId)) {
            Vintage vintage = vintageRepository.getReferenceById(vintageId);
            Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException.MemberNotFoundException::new);

            try {
                vintageLikeRepository.save(VintageLike.create(vintage, member));

            } catch (DataIntegrityViolationException e) {
                // 동시성으로 UNIQUE 제약 위반 → 이미 좋아요가 존재한다고 판단(멱등)
                log.error(e.getMessage());
                throw new DataIntegrityViolationException(e.getMessage());
            }
        }
        long count = vintageLikeRepository.countLikesByVintageId(vintageId); // 또는 캐시/카운터 사용
        return new VintageLikeResponse.VintageLike(true, count);
    }

    @Transactional
    public VintageLikeResponse.VintageLike unlike(Long vintageId) {
        Vintage vintage = vintageRepository.getReferenceById(vintageId);
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId())
                .orElseThrow(MemberException.MemberNotFoundException::new);

        long deleted = vintageLikeRepository.deleteByVintageAndMember(vintage, member);

        long count = vintageLikeRepository.countLikesByVintageId(vintageId);
        return new VintageLikeResponse.VintageLike(false, count);
    }

    @Transactional(readOnly = true)
    public VintageLikeResponse.VintageLike getStatus(Long vintageId) {
        boolean liked = vintageLikeRepository.existsLikeByVintageIdAndMemberId(vintageId, SecurityUtil.getCurrentMemberId());
        long count = vintageLikeRepository.countLikesByVintageId(vintageId);

        return new VintageLikeResponse.VintageLike(liked, count);
    }



}
