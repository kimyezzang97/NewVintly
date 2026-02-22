package com.vintly.infra.vintagecomment;

import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintagecomment.entity.VintageComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VintageCommentJpaRepository extends JpaRepository<VintageComment, Long> {

    void deleteAllByVintage(Vintage vintage);

    void deleteAllByParentCommentId(Long parentCommentId);
}
