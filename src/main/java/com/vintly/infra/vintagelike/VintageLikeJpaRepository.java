package com.vintly.infra.vintagelike;

import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintagelike.entity.VintageLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VintageLikeJpaRepository extends JpaRepository<VintageLike, Long> {

    void deleteAllByVintage(Vintage vintage);
}
