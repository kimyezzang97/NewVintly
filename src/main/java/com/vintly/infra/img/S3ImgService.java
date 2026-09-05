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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class S3ImgService implements ImgService {

    // 확장자로 인정할 형태 (점 + 영숫자 1~10자). 벗어나면 확장자 없이 저장한다.
    private static final Pattern EXTENSION_PATTERN = Pattern.compile("^\\.[a-z0-9]{1,10}$");

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
        // 키 생성은 예외를 던지지 않으므로 try 밖에서 만든다. 실패 로그에 키를 함께 남기기 위함.
        String extension = extractExtension(image.getOriginalFilename()); // 확장자 추출
        String fileName = buildFileName(path, extension); // S3에 저장할 파일명 생성

        try {
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
            // 원인(e)을 반드시 물려준다. 빠뜨리면 상위에 남는 스택트레이스에 실제 사유가 사라져
            // NoSuchBucket 같은 설정 문제를 로그를 따로 뒤져야만 알 수 있다.
            log.error("S3 이미지 업로드 실패 - bucket: {}, key: {}", bucket, fileName, e);
            throw new RuntimeException("S3 이미지 업로드 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 원본 파일명에서 확장자를 뽑는다. 뽑을 수 없으면 빈 문자열.
     *
     * <p>확장자가 없어도 업로드 시 Content-Type을 함께 저장하므로 이미지 서빙에는 지장이 없다.
     * 다만 다운로드나 S3 콘솔 확인을 위해 살릴 수 있으면 살린다.
     *
     * <p>파일명은 클라이언트가 보내는 값이라 그대로 믿고 자르면 안 된다.
     * 점이 없으면 substring(-1)로 예외가 나고, 점 뒤에 임의 문자열이 오면 S3 키에 그대로 섞여 들어간다.
     */
    private String extractExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return ""; // 점이 없거나 점으로 끝나는 경우
        }

        String extension = originalFilename.substring(dotIndex).toLowerCase();
        return EXTENSION_PATTERN.matcher(extension).matches() ? extension : "";
    }

    private String buildFileName(String path, String extension) {
        String now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_")); // 날짜+시간 포맷
        return String.format("images/%s/%s%s%s",
                path,           // ex) vintage, board, notice
                now, // ex) 20250625_145922
                UUID.randomUUID().toString().substring(0, 8), // UUID 일부, // 중복 방지를 위한 고유 식별자
                extension); // .jpg, .png 등
    }

    @Override
    public void deleteImgList(List<String> imgList) {
        for (String fullUrl : imgList) {
            try {
                // s3Domain 제거하여 S3 내부 key 추출
                String key = fullUrl.replace(s3Domain + "/", "");

                s3Client.deleteObject(builder -> builder
                        .bucket(bucket)
                        .key(key)
                        .build());

            } catch (Exception e) {
                log.error("S3 이미지 삭제 실패: {}", fullUrl, e);
            }
        }
    }
}
