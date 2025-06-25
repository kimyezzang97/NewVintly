package com.vintly.domain.vintageimg.repo;

import com.vintly.domain.vintageimg.entity.VintageImg;
import org.springframework.stereotype.Repository;

@Repository
public interface VintageImgRepository {

    void save(VintageImg vintageImg);
}
