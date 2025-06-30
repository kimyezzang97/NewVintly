package com.vintly.domain.vintageimg.repo;

import com.vintly.domain.vintageimg.entity.VintageImg;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VintageImgRepository {

    void save(VintageImg vintageImg);

    void saveAll(List<VintageImg> vintageImgList);
}
