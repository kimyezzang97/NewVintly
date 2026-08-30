package com.vintly.infra.vintageimg;

import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintageimg.entity.VintageImg;
import com.vintly.domain.vintageimg.repo.VintageImgRepository;
import com.vintly.infra.vintage.VintageJpaRepository;
import com.vintly.infra.vintage.VintageQueryDslRepository;
import com.vintly.interfaces.vintage.VintageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VintageImgRepositoryImpl implements VintageImgRepository {

    private final VintageImgJpaRepository vintageImgJpaRepository;
    private final VintageImgQueryDslRepository queryDslRepository;

    @Autowired
    public VintageImgRepositoryImpl(VintageImgJpaRepository vintageImgJpaRepository, VintageImgQueryDslRepository queryDslRepository) {
        this.vintageImgJpaRepository = vintageImgJpaRepository;
        this.queryDslRepository = queryDslRepository;
    }

    @Override
    public void save(VintageImg vintageImg) {
        vintageImgJpaRepository.save(vintageImg);
    }

    @Override
    public List<VintageImg> saveAll(List<VintageImg> vintageImgList) {
        return vintageImgJpaRepository.saveAll(vintageImgList);
    }

    // 빈티지 매장 이미지 경로 리스트 조회
    @Override
    public List<String> findImagePathListByVintageId(Long vintageId) {
        return queryDslRepository.findImagePathListByVintageId(vintageId);
    }

    // 해당 빈티지 매장의 (이미지 경로, 이미지 id) 리스트 조회
    @Override
    public List<VintageInfo.Image> findImgListByVintageId(Long vintageId) {
        return queryDslRepository.findImgListByVintageId(vintageId);
    }

    // 해당 빈티지 매장의 이미지 엔티티 리스트 조회
    @Override
    public List<VintageImg> findImgEntityListByVintageId(Long vintageId) {
        return queryDslRepository.findImgEntityListByVintageId(vintageId);
    }

    // 빈티지 이미지 리스트 삭제
    @Override
    public void deleteAll(List<VintageImg> list) {
        vintageImgJpaRepository.deleteAll(list);
    }

}
