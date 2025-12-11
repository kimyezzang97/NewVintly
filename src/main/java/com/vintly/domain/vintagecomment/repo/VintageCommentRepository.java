package com.vintly.domain.vintagecomment.repo;

import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintagecomment.entity.VintageComment;

import java.util.List;
import java.util.Optional;

public interface VintageCommentRepository {

    // 해당 빈티지 매장의 댓글 리스트 조회
    List<VintageInfo.Comment> findCommentsByVintageId(Long vintageId);

    void deleteAllCommentsByVintageId(Vintage vintage);

    // 저장
    VintageComment save(VintageComment entity);

    Optional<VintageComment> findById(Long vintageCommentId);
}
