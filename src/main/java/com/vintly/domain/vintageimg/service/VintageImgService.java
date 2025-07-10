package com.vintly.domain.vintageimg.service;

import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintageimg.repo.VintageImgRepository;
import com.vintly.interfaces.vintage.VintageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
