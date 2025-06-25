package com.vintly.infra.vintageimg;

import com.vintly.domain.vintageimg.entity.VintageImg;
import com.vintly.domain.vintageimg.repo.VintageImgRepository;
import com.vintly.infra.vintage.VintageJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class VintageImgRepositoryImpl implements VintageImgRepository {

    private final VintageImgJpaRepository vintageImgJpaRepository;

    public VintageImgRepositoryImpl(VintageImgJpaRepository vintageImgJpaRepository) {
        this.vintageImgJpaRepository = vintageImgJpaRepository;
    }

    @Override
    public void save(VintageImg vintageImg) {
        vintageImgJpaRepository.save(vintageImg);
    }

}
