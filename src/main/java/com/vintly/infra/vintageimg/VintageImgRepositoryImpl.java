package com.vintly.infra.vintageimg;

import com.vintly.domain.vintageimg.entity.VintageImg;
import com.vintly.domain.vintageimg.repo.VintageImgRepository;
import com.vintly.infra.vintage.VintageJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VintageImgRepositoryImpl implements VintageImgRepository {

    private final VintageImgJpaRepository vintageImgJpaRepository;

    @Autowired
    public VintageImgRepositoryImpl(VintageImgJpaRepository vintageImgJpaRepository) {
        this.vintageImgJpaRepository = vintageImgJpaRepository;
    }

    @Override
    public void save(VintageImg vintageImg) {
        vintageImgJpaRepository.save(vintageImg);
    }

    @Override
    public void saveAll(List<VintageImg> vintageImgList) {
        vintageImgJpaRepository.saveAll(vintageImgList);
    }

}
