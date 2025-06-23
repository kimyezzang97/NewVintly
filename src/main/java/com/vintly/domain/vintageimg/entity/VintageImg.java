package com.vintly.domain.vintageimg.entity;

import com.vintly.domain.BaseEntity;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.vintage.entity.Vintage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // JPA 생성 시간 자동 적용
@Entity(name = "vintage_img")
public class VintageImg extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vintage_img_id")
    private Long vintageImgId;

    @Comment("VINTAGE 외래 키")
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩으로 성능 관리, DB에서 진짜 필요한 것만 쿼리로 날림.
    @JoinColumn(name = "vintage_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)) // FK 제약 X
    private Vintage vintage;

    @Comment("이미지 경로")
    private String img_path;
}
