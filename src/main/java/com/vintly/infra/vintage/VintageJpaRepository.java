package com.vintly.infra.vintage;

import com.vintly.domain.vintage.entity.Vintage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VintageJpaRepository extends JpaRepository<Vintage, Long> {

}
