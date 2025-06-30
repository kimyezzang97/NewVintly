package com.vintly.domain.vintage.entity;

import com.vintly.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // JPA 생성 시간 자동 적용
@Entity(name = "vintage")
public class Vintage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vintage_id")
    private Long vintageId;

    @Comment("빈티지 매장 이름")
    @Column(nullable = false)
    private String name;

    @Comment("큰주소 ex) 경기도, 서울시")
    @Column(nullable = false)
    private String state;

    @Comment("중간주소 ex) 군포시, 강동구(서울 경우)")
    @Column(nullable = false)
    private String district;

    @Comment("상세주소 ex) 아차산로 302")
    @Column(nullable = false, name = "detail_addr")
    private String detailAddr;

    @Comment("위도 ex) 37.566535")
    @Column(nullable = false, precision = 9, scale = 6) // precision : 전체 자리수, scale : 소수점 이하 자리수
    private BigDecimal lat;

    @Comment("경도 ex) 126.977969")
    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal  lon;

    @Comment("대표 이미지 ID (FK)")
    @Column(name = "vintage_imgae_id")
    private Long vintageImageId;

    // 생성자 대신 정적 팩토리 메서드
    public static Vintage create(String name, String state, String district, String detailAddr,
                                 BigDecimal lat, BigDecimal lon, Long vintageImageId) {
        Vintage vintage = new Vintage();
        vintage.name = name;
        vintage.state = state;
        vintage.district = district;
        vintage.detailAddr = detailAddr;
        vintage.lat = lat;
        vintage.lon = lon;
        vintage.vintageImageId = vintageImageId;
        return vintage;
    }

    // 대표 이미지 적용
    public void updateThumbnail(Long newThumbnailId) {
        if (newThumbnailId == null) {
            throw new IllegalArgumentException("대표 이미지 ID는 null일 수 없습니다.");
        }
        this.vintageImageId = newThumbnailId;
    }
}
