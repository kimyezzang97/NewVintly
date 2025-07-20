package com.vintly.domain.vintagelike.service;

import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintagelike.repo.VintageLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VintageLikeService {

    private final VintageLikeRepository vintageLikeRepository;

    // 해당 빈티지 매장의 좋아요 수 카운트
    public int countLikesByVintageId(Long vintageId) {
        return vintageLikeRepository.countLikesByVintageId(vintageId);
    }

    // 특정 유저가 해당 매장에 좋아요를 눌렀는지 여부 확인
    public boolean existsLikeByVintageIdAndMemberId(Long vintageId, Long memberId){
        return vintageLikeRepository.existsLikeByVintageIdAndMemberId(vintageId, memberId);
    }

    public void deleteAllLikesByVintage(Vintage vintage){
        vintageLikeRepository.deleteAllLikesByVintage(vintage);
    }
}
