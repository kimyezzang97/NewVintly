package com.vintly.domain.vintagecomment.repo;

import com.vintly.domain.vintage.dto.VintageInfo;

import java.util.List;

public interface VintageCommentRepository {

    // 해당 빈티지 매장의 댓글 리스트 조회
    List<VintageInfo.Comment> findCommentsByVintageId(Long vintageId);


}
