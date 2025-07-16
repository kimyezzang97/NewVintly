package com.vintly.domain.vintageimg.service;

import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintageimg.entity.VintageImg;
import com.vintly.domain.vintageimg.repo.VintageImgRepository;
import com.vintly.interfaces.vintage.VintageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VintageImgService {

    private final VintageImgRepository vintageImgRepository;

    // 해당 빈티지 매장의 이미지 경로 리스트 조회
    public List<String> findImagePathListByVintageId(Long vintageId){
        return vintageImgRepository.findImagePathListByVintageId(vintageId);
    }

    // 해당 빈티지 매장의 (이미지 경로, 이미지 id) 리스트 조회
    public List<VintageInfo.Image> findImgListByVintageId(Long vintageId){
        return vintageImgRepository.findImgListByVintageId(vintageId);
    }

    // DB에 빈티지 매장 img list 저장
    public List<VintageImg> saveImgList(List<String> imgUrls, Vintage vintage) {
        List<VintageImg> vintageImgList = new ArrayList<>();
        for (String imgUrl : imgUrls) {
            VintageImg vintageImg = VintageImg.create(vintage, imgUrl);
            vintageImgList.add(vintageImg);
        }

        return vintageImgRepository.saveAll(vintageImgList);
    }
}
