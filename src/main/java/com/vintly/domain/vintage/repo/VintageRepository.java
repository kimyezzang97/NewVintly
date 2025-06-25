package com.vintly.domain.vintage.repo;

import com.vintly.domain.vintage.entity.Vintage;
import org.springframework.stereotype.Repository;

@Repository
public interface VintageRepository {

    void save(Vintage vintage);


}
