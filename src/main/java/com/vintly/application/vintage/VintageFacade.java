package com.vintly.application.vintage;

import com.vintly.domain.img.service.ImgService;
import com.vintly.domain.member.service.CustomUserDetails;
import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintage.repo.VintageRepository;
import com.vintly.domain.vintage.service.VintageService;
import com.vintly.domain.vintagecomment.service.VintageCommentService;
import com.vintly.domain.vintageimg.entity.VintageImg;
import com.vintly.domain.vintageimg.repo.VintageImgRepository;
import com.vintly.domain.vintageimg.service.VintageImgService;
import com.vintly.domain.vintagelike.repo.VintageLikeRepository;
import com.vintly.domain.vintagelike.service.VintageLikeService;
import com.vintly.interfaces.vintage.VintageException;
import com.vintly.interfaces.vintage.VintageRequest;
import com.vintly.interfaces.vintage.VintageResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
@RequiredArgsConstructor
public class VintageFacade {

    private final ImgService imgService;
    private final VintageRepository vintageRepository;
    private final VintageImgRepository vintageImgRepository;
    private final VintageLikeService vintageLikeService;
    private final VintageImgService vintageImgService;
    private final VintageCommentService vintageCommentService;
    private final VintageService vintageService;


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


    // 빈티지 매장 상세 조회
    @Transactional(readOnly = true)
    public VintageResponse.Vintage getVintage(Long vintageId) {
        // 1. 기본 정보 조회 (엔티티)
        Vintage vintage = vintageService.findBasicInfoById(vintageId)
                .orElseThrow(() -> new EntityNotFoundException("해당 빈티지 매장을 찾을 수 없습니다."));

        // 2. 이미지 리스트 조회
        List<String> imagePaths = vintageImgService.findImagePathListByVintageId(vintageId);

        // 3. 좋아요 수 조회
        int likeCount = vintageLikeService.countLikesByVintageId(vintageId);

        // 4. 사용자 좋아요 여부 확인

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        System.out.println("email : " + email);

//        boolean liked = vintageLikeService.existsLikeByVintageIdAndMemberId(vintageId, memberId);
//
//        // 5. 댓글 리스트 조회
//        List<VintageInfo.Comment> comments = vintageCommentService.findCommentsByVintageId(vintageId);
//
//        // 결과 조합 및 반환
//        return new VintageInfo.VintageDetail(
//                vintage.getVintageId(),
//                vintage.getName(),
//                vintage.getState(),
//                vintage.getDistrict(),
//                vintage.getDetailAddr(),
//                vintage.getLat(),
//                vintage.getLon(),
//                imagePaths,
//                likeCount,
//                liked,
//                comments
//        );

        return null;
    }
}
