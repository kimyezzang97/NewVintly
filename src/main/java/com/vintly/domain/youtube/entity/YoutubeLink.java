package com.vintly.domain.youtube.entity;

import com.vintly.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity(name = "youtube_link")
public class YoutubeLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "youtube_link_id")
    private Long youtubeLinkId;

    @Comment("유튜브 영상 URL")
    @Column(nullable = false, length = 500)
    private String url;

    @Comment("제목")
    @Column(nullable = false)
    private String title;

    @Comment("설명")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Comment("광고 여부 (0=false, 1=true)")
    @Column(name = "is_ad", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isAd;
}
