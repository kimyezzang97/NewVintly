package com.vintly.application.vintage;

import com.vintly.domain.block.service.MemberBlockService;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


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
    private final MemberBlockService memberBlockService;



    // 빈티지 매장 등록
    @CacheEvict(cacheNames = "vintages", allEntries = true)
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
            throw new VintageException.VintageCreateException(e);
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

        // 5. 댓글 리스트 조회 (비로그인이면 차단 목록이 없으므로 필터가 붙지 않는다)
        List<Long> blockedIds = memberId != null
                ? memberBlockService.findBlockedIds(memberId)
                : List.of();
        List<VintageInfo.Comment> result  = vintageCommentService.findCommentsByVintageId(vintageId, blockedIds);

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

    // 빈티지 매장 수정
    @CacheEvict(cacheNames = "vintages", allEntries = true)
    @Transactional
    public void updateVintage(Long vintageId, VintageRequest.UpdateVintage request) {
        Vintage vintage = vintageService.findBasicInfoById(vintageId)
                .orElseThrow(() -> new EntityNotFoundException("해당 빈티지 매장을 찾을 수 없습니다."));

        // 1. 기본 정보 수정
        vintage.updateInfo(
                request.name(),
                request.state(),
                request.district(),
                request.detailAddr(),
                request.lat(),
                request.lon()
        );

        updateVintageImg(vintage, request);
    }

    // 빈티지 매장 이미지 수정(DB, S3)
    private void updateVintageImg(Vintage vintage, VintageRequest.UpdateVintage request) {
        // 삭제하지 않고 남기기로 한 이미지 리스트
        List<Long> remainImgList = Optional.ofNullable(request.remainImageIdList()).orElse(List.of());

        // 기존 이미지 조회
        List<VintageImg> existingImgList = vintageImgService.findImgEntityListByVintage(vintage);

        // 삭제 원하는 이미지들 선별
        List<VintageImg> toDelete = existingImgList.stream()
                .filter(img -> !remainImgList.contains(img.getVintageImgId()))
                .toList();

        // DB 삭제
        vintageImgService.deleteAll(toDelete);

        // S3 삭제
        List<String> deleteImgPaths = toDelete.stream()
                .map(VintageImg::getImgPath)
                .toList();
        imgService.deleteImgList(deleteImgPaths);

        // 새 이미지 S3 업로드
        List<String> uploadedImgPathList = imgService.uploadImgList(request.imgList(), "vintage"); // /vintages/ 경로에

        // 엔티티 생성 및 저장
        List<VintageImg> vintageImgList = vintageImgService.saveImgList(uploadedImgPathList, vintage);

        // 대표 이미지 갱신
        if (!vintageImgList.isEmpty()) {
            vintage.updateThumbnail(vintageImgList.get(0).getVintageImgId());
        }
    }

    // 빈티지 매장 삭제
    @CacheEvict(cacheNames = "vintages", allEntries = true)
    public void deleteVintage(Long vintageId){
        // 1. 빈티지 조회
        Vintage vintage = vintageService.findBasicInfoById(vintageId)
                .orElseThrow(() -> new EntityNotFoundException("해당 빈티지 매장을 찾을 수 없습니다."));

        // 2. 댓글 삭제
        vintageCommentService.deleteAllCommentsByVintage(vintage);

        // 3. 좋아요 삭제
        vintageLikeService.deleteAllLikesByVintage(vintage);

        // 4. 이미지 처리
        List<VintageImg> vintageImgList = vintageImgService.findImgEntityListByVintage(vintage);

        List<String> imgPathList = vintageImgList.stream()
                .map(VintageImg::getImgPath)
                .toList();

        imgService.deleteImgList(imgPathList); // S3
        vintageImgService.deleteAll(vintageImgList); // DB

        // 5. 빈티지 삭제
        vintageService.delete(vintage);
    }

}
