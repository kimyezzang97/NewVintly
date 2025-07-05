package com.vintly.domain.vintage.service;

import com.vintly.domain.img.service.ImgService;
import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintage.repo.VintageRepository;
import com.vintly.domain.vintageimg.entity.VintageImg;
import com.vintly.domain.vintageimg.repo.VintageImgRepository;
import com.vintly.interfaces.vintage.VintageException;
import com.vintly.interfaces.vintage.VintageRequest;
import com.vintly.interfaces.vintage.VintageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class VintageService {

    private final ImgService imgService;
    private final VintageRepository vintageRepository;
    private final VintageImgRepository vintageImgRepository;

    @Autowired
    public VintageService(ImgService imgService, VintageRepository vintageRepository, VintageImgRepository vintageImgRepository) {
        this.imgService = imgService;
        this.vintageRepository = vintageRepository;
        this.vintageImgRepository = vintageImgRepository;
    }

    // 빈티지 매장 전체 조회
    @Transactional(readOnly = true)
    public List<VintageResponse.VintageList> getVintageList() {

        return vintageRepository.getVintageList().stream()
                .map(v -> new VintageResponse.VintageList(
                        v.vintageId(),
                        v.name(),
                        v.state(),
                        v.district(),
                        v.detailAddr(),
                        v.lat(),
                        v.lon(),
                        v.thumbnailPath()
                )).toList();
    }
}
