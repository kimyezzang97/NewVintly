package com.vintly.application.vintage;

import com.vintly.domain.img.service.ImgService;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.domain.member.service.CustomUserDetails;
import com.vintly.domain.member.service.MemberService;
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
import com.vintly.infra.util.SecurityUtil;
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
import java.util.Optional;


@Component
@Slf4j
@RequiredArgsConstructor
public class VintageFacade {

    private final ImgService imgService;
    private final VintageLikeService vintageLikeService;
    private final VintageImgService vintageImgService;
    private final VintageCommentService vintageCommentService;
    private final VintageService vintageService;
    private final MemberService memberService;



    // 빈티지 매장 등록
    @Transactional(rollbackFor = Exception.class)
    public void createVintage(VintageRequest.CreateVintage createVintage) {
        try {
            List<MultipartFile> files = createVintage.images();

            // S3 모든 이미지 업로드
            List<String> imgUrls = imgService.uploadImgList(files, "vintage"); // /vintages/ 경로에

            // Vintage 매장 생성 - 대표 이미지 ID는 저장 후 가져옴
            Vintage vintage = vintageService.createVintage(createVintage);

            // 이미지 엔티티 생성
            List<VintageImg> vintageImgList = vintageImgService.saveImgList(imgUrls, vintage);

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
        List<VintageInfo.Image> imgList = vintageImgService.findImgListByVintageId(vintageId);

        // 3. 좋아요 수 조회
        int likeCount = vintageLikeService.countLikesByVintageId(vintageId);

        // 4. 사용자 좋아요 여부 확인
        String email = SecurityUtil.getCurrentEmail();
        Optional<Member> optMember = memberService.findByEmail(email);
        Long memberId = optMember.map(Member::getMemberId).orElse(null);

        boolean liked = false;
        if (memberId != null) {
            liked = vintageLikeService.existsLikeByVintageIdAndMemberId(vintageId, memberId);
        }

        // 5. 댓글 리스트 조회
        List<VintageInfo.Comment> result  = vintageCommentService.findCommentsByVintageId(vintageId);

        // 6. 내부 전용 DTO → 응답용 DTO로 매핑 후 반환
        VintageInfo.VintageDetail detail = new VintageInfo.VintageDetail(
                vintage.getVintageId(),
                vintage.getName(),
                vintage.getState(),
                vintage.getDistrict(),
                vintage.getDetailAddr(),
                vintage.getLat(),
                vintage.getLon(),
                imgList,
                likeCount,
                liked,
                result
        );

        return VintageResponse.Vintage.from(detail);
    }

//    // 빈티지 매장 수정
//    @Transactional
//    public void updateVintage(Long vintageId, VintageRequest.UpdateVintage request) {
//        Vintage vintage = vintageService.findByIdOrThrow(vintageId);
//
//        // 1. 기본 정보 수정
//        vintage.updateInfo(
//                request.name(),
//                request.state(),
//                request.district(),
//                request.detailAddr(),
//                request.lat(),
//                request.lon()
//        );
//
//        // 2. 기존 이미지 중 삭제 대상 찾기
//        List<VintageImg> currentImages = vintageImgRepository.findByVintage(vintage);
//        List<Long> remainIds = Optional.ofNullable(request.remainImageIds()).orElse(List.of());
//
//        List<VintageImg> toDelete = currentImages.stream()
//                .filter(img -> !remainIds.contains(img.getVintageImgId()))
//                .toList();
//
//        toDelete.forEach(img -> {
//            vintageImgService.delete(img.getImgPath()); // S3 삭제
//            vintageImgRepository.delete(img);
//        });
//
//        // 3. 새 이미지 업로드
//        List<String> newImgPaths = vintageImgService.uploadList(request.newImages(), "vintage");
//        List<VintageImg> newImgs = newImgPaths.stream()
//                .map(path -> VintageImg.create(vintage, path))
//                .toList();
//        vintageImgRepository.saveAll(newImgs);
//
//        // 4. 대표 이미지 재설정
//        List<Long> updatedImgIds = Stream.concat(
//                currentImages.stream()
//                        .filter(img -> remainIds.contains(img.getVintageImgId()))
//                        .map(VintageImg::getVintageImgId),
//                newImgs.stream()
//                        .map(VintageImg::getVintageImgId)
//        ).toList();
//
//        if (!updatedImgIds.isEmpty()) {
//            vintage.updateThumbnail(updatedImgIds.get(0));
//        }
//    }
}
