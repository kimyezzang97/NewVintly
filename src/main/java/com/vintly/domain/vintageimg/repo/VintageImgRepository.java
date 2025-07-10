package com.vintly.domain.vintageimg.repo;

import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintageimg.entity.VintageImg;
import com.vintly.interfaces.vintage.VintageResponse;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VintageImgRepository {

    void save(VintageImg vintageImg);

    void saveAll(List<VintageImg> vintageImgList);

    // 해당 빈티지 매장의 이미지 경로 리스트 조회
    List<String> findImagePathListByVintageId(Long vintageId);

    // 해당 빈티지 매장의 (이미지 경로, 이미지 id) 리스트 조회
    List<VintageInfo.Image> findImgListByVintageId(Long vintageId);
}
