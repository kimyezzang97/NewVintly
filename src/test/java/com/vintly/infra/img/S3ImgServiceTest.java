package com.vintly.infra.img;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * S3 키 생성 규칙 테스트.
 *
 * 확장자가 없어도 업로드 시 Content-Type을 함께 저장하므로 이미지 서빙 자체는 문제없지만,
 * 원본 파일명의 확장자가 조용히 버려지던 문제(String.format 지정자 부족)를 막는다.
 */
@ExtendWith(MockitoExtension.class)
class S3ImgServiceTest {

    private static final String BUCKET = "test-bucket";
    private static final String DOMAIN = "https://cdn.test";

    @Mock
    private S3Client s3Client;

    private S3ImgService s3ImgService;

    @BeforeEach
    void setUp() {
        s3ImgService = new S3ImgService(s3Client);
        ReflectionTestUtils.setField(s3ImgService, "bucket", BUCKET);
        ReflectionTestUtils.setField(s3ImgService, "s3Domain", DOMAIN);
    }

    @Test
    @DisplayName("원본 파일명의 확장자가 S3 키와 반환 URL에 유지된다. 대문자는 소문자로 통일한다.")
    void keepsExtensionInS3Key() {
        // Arrange
        MultipartFile image = new MockMultipartFile(
                "images", "shop.JPG", "image/jpeg", "dummy".getBytes());

        // Act
        List<String> urls = s3ImgService.uploadImgList(List.of(image), "vintage");

        // Assert
        assertThat(capturedKey()).startsWith("images/vintage/").endsWith(".jpg");
        assertThat(urls.get(0)).startsWith(DOMAIN + "/images/vintage/").endsWith(".jpg");
    }

    @Test
    @DisplayName("확장자가 없는 파일명이어도 예외 없이 업로드된다.")
    void uploadsWithoutExtensionWhenFilenameHasNoDot() {
        // Arrange - 예전에는 substring(-1)로 StringIndexOutOfBoundsException이 났다
        MultipartFile image = new MockMultipartFile(
                "images", "shop", "image/jpeg", "dummy".getBytes());

        // Act
        List<String> urls = s3ImgService.uploadImgList(List.of(image), "vintage");

        // Assert
        assertThat(capturedKey()).startsWith("images/vintage/").doesNotContain(".");
        assertThat(urls).hasSize(1);
    }

    @Test
    @DisplayName("확장자 형태가 아닌 파일명은 S3 키에 섞여 들어가지 않는다.")
    void ignoresSuspiciousExtension() {
        // Arrange - 파일명은 클라이언트가 보내는 값이라 그대로 믿을 수 없다
        MultipartFile image = new MockMultipartFile(
                "images", "shop.jpg/../../etc/passwd", "image/jpeg", "dummy".getBytes());

        // Act
        s3ImgService.uploadImgList(List.of(image), "vintage");

        // Assert
        assertThat(capturedKey())
                .startsWith("images/vintage/")
                .doesNotContain("..")
                .doesNotContain("passwd");
    }

    private String capturedKey() {
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        return captor.getValue().key();
    }
}
