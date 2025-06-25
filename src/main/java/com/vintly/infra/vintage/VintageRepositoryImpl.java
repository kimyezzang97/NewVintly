package com.vintly.infra.vintage;

import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintage.repo.VintageRepository;
import org.springframework.stereotype.Repository;

@Repository
public class VintageRepositoryImpl implements VintageRepository {

    private final VintageJpaRepository vintageJpaRepository;

    public VintageRepositoryImpl(VintageJpaRepository vintageJpaRepository) {
        this.vintageJpaRepository = vintageJpaRepository;
    }

    @Override
    public void save(Vintage vintage) {
        vintageJpaRepository.save(vintage);
    }
}
