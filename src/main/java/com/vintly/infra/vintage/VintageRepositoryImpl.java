package com.vintly.infra.vintage;

import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintage.repo.VintageRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VintageRepositoryImpl implements VintageRepository {

    private final VintageJpaRepository vintageJpaRepository;
    private final VintageQueryDslRepository vintageQueryDslRepository;
    public VintageRepositoryImpl(VintageJpaRepository vintageJpaRepository, VintageQueryDslRepository vintageQueryDslRepository) {
        this.vintageJpaRepository = vintageJpaRepository;
        this.vintageQueryDslRepository = vintageQueryDslRepository;
    }

    @Override
    public Vintage save(Vintage vintage) {
        return vintageJpaRepository.save(vintage);
    }

    // 빈티지 매장 리스트 조회
    @Override
    public List<VintageInfo.Vintage> getVintageList() {
        return vintageQueryDslRepository.getAllVintageList();
    }

    // 빈티지 기본 정보 조회
    @Override
    public Optional<Vintage> findBasicInfoById(Long vintageId) {
        return vintageQueryDslRepository.findBasicInfoById(vintageId); //
    }

    @Override
    public void delete(Vintage vintage) {
        vintageJpaRepository.delete(vintage);
    }

    @Override
    public List<VintageInfo.Vintage> findByLocation(String state, String district) {
        return vintageQueryDslRepository.findByLocation(state, district);
    }

}
