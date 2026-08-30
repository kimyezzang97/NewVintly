package com.vintly.domain.vintage.repo;

import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintage.entity.Vintage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VintageRepository {

    Vintage save(Vintage vintage);

    // 빈티지 매장 리스트 조회
    List<VintageInfo.Vintage> getVintageList();

    // 빈티지 기본 정보 조회
    Optional<Vintage> findBasicInfoById(Long vintageId);

    void delete(Vintage vintage);

    // 지역 명으로 빈티지 매장 리스트 조회
    List<VintageInfo.Vintage> findByLocation(String state, String district);

    // 조회 쿼리 없이 프록시로 참조만 걸기
    Vintage getReferenceById(Long vintageId);
}
