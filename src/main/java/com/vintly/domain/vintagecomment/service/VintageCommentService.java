package com.vintly.domain.vintagecomment.service;

import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintagecomment.repo.VintageCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VintageCommentService {

    private final VintageCommentRepository vintageCommentRepository;

    // 빈티지 매장에 달린 댓글 리스트 조회 (최신순 정렬)
    public List<VintageInfo.Comment> findCommentsByVintageId(Long vintageId) {
        return vintageCommentRepository.findCommentsByVintageId(vintageId);
    }

    public void deleteAllCommentsByVintage(Vintage vintage) {
        vintageCommentRepository.deleteAllCommentsByVintageId(vintage);
    }
}
