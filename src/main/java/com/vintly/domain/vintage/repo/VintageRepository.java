package com.vintly.domain.vintage.repo;

import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintage.entity.Vintage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VintageRepository {

    void save(Vintage vintage);

    List<VintageInfo.Vintage> getVintageList();
}
