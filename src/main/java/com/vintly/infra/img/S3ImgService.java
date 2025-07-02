package com.vintly.infra.img;

import com.vintly.domain.img.service.ImgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class S3ImgService implements ImgService {

    private final S3Client s3Client;

    @Deprecated
    public S3ImgService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Value("${cloud.aws.s3.bucket}")
    private String bucket; // S3 버킷 이름

    @Value("${cloud.aws.s3.domain}") // ex) https://{bucket}.s3.amazonaws.com
    private String s3Domain;

    @Override
    public List<String> uploadImgList(List<MultipartFile> images, String path) {

        return images.stream()
                .map(image -> uploadSingleImage(image, path)) // 개별 이미지 업로드
                .collect(Collectors.toList()); // 업로드된 이미지 URL 리스트 반환
    }

    private String uploadSingleImage(MultipartFile image, String path) {
        try {
            String originalFilename = image.getOriginalFilename(); // 원본 파일명 추출
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.')); // 확장자 추출
            String fileName = buildFileName(path, extension); // S3에 저장할 파일명 생성

            // S3에 업로드
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket) // 업로드할 S3 버킷
                            .key(fileName)  // S3에 저장될 경로 및 파일명
                            .contentType(image.getContentType()) // 파일의 MIME 타입
                            .build(),
                    RequestBody.fromBytes(image.getBytes()) // 실제 파일 데이터
            );

            return s3Domain + "/" + fileName; // 접근 가능한 URL 리턴

        } catch (Exception e) {
            log.error("S3 이미지 업로드 실패", e);
            throw new RuntimeException("S3 이미지 업로드 중 오류가 발생했습니다.");
        }
    }

    private String buildFileName(String path, String extension) {
        String now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_")); // 날짜+시간 포맷
        return String.format("images/%s/%s%s",
                path,           // ex) vintage, board, notice
                now, // ex) 20250625_145922
                UUID.randomUUID().toString().substring(0, 8), // UUID 일부, // 중복 방지를 위한 고유 식별자
                extension); // .jpg, .png 등
    }

}
