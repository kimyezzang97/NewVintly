package com.vintly.domain.vintage.service;

import com.vintly.domain.img.service.ImgService;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintage.repo.VintageRepository;
import com.vintly.domain.vintageimg.entity.VintageImg;
import com.vintly.domain.vintageimg.repo.VintageImgRepository;
import com.vintly.interfaces.vintage.VintageException;
import com.vintly.interfaces.vintage.VintageRequest;
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

    // 빈티지 매장 등록
    @Transactional(rollbackFor = Exception.class)
    public void createVintage(VintageRequest.CreateVintage createVintage) {
        try {
            List<MultipartFile> files = createVintage.images();

            // 모든 이미지 업로드
            List<String> imgUrls = imgService.uploadImgList(files, "vintage"); // /vintages/ 경로에

            // Vintage 매장 생성
            // 대표 이미지 ID는 저장 후 가져옴
            Vintage vintage = Vintage.create(
                    createVintage.name(),
                    createVintage.state(),
                    createVintage.district(),
                    createVintage.detailAddr(),
                    createVintage.lat(),
                    createVintage.lon(),
                    null // 나중에 대표 이미지 ID 세팅
            );

            vintageRepository.save(vintage);

            // 이미지 엔티티 생성
            List<VintageImg> vintageImgList = new ArrayList<>();
            for (String imgUrl : imgUrls) {
                VintageImg vintageImg = VintageImg.create(vintage, imgUrl);
                vintageImgList.add(vintageImg);
            }

            vintageImgRepository.saveAll(vintageImgList);

            // 대표 이미지 등록
            vintage.updateThumbnail(vintageImgList.get(0).getVintageImgId());

        } catch (Exception e) {
            log.error("빈티지 매장 등록 중 오류 발생", e);
            throw new VintageException.VintageCreateException();
        }

    }
}
