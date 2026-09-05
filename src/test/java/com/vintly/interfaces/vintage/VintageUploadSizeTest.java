package com.vintly.interfaces.vintage;

import com.vintly.application.vintage.VintageFacade;
import com.vintly.domain.vintage.service.VintageService;
import com.vintly.interfaces.presentation.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * 업로드 용량 초과가 전역 핸들러에서 413으로 매핑되는지 검증한다.
 *
 * 실제 운영에서 이 예외는 컨트롤러 진입 전 멀티파트 파싱 단계에서 터진다
 * (그래서 VintageFacade.createVintage 의 try-catch 에는 잡히지 않는다).
 * standalone MockMvc 에는 MultipartResolver 를 갈아끼우는 훅이 없어 그 단계를 재현할 수 없으므로,
 * 여기서는 예외가 던져졌을 때의 매핑 결과(code/msg)만 검증한다.
 * 파싱 단계 예외가 @RestControllerAdvice 로 전달되는 것 자체는 스프링의 기본 동작이다.
 *
 * 이 프로젝트는 예외를 HTTP 상태 코드가 아니라 ApiResponse.code 로 돌려주므로
 * (전역 핸들러에 @ResponseStatus 가 없다) 본문의 code 를 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class VintageUploadSizeTest {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    @Mock private VintageService vintageService;
    @Mock private VintageFacade vintageFacade;
    @InjectMocks private VintageController vintageController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(vintageController)
                .setControllerAdvice(new GlobalExceptionHandler("10MB", "100MB"))
                .build();
    }

    @Test
    @DisplayName("업로드 용량을 초과하면 500이 아니라 code 413과 허용 용량 안내를 반환한다.")
    void createVintageReturns413WhenUploadTooLarge() throws Exception {
        // Arrange
        willThrow(new MaxUploadSizeExceededException(MAX_FILE_SIZE_BYTES))
                .given(vintageFacade).createVintage(any());

        MockMultipartFile image = new MockMultipartFile(
                "images", "shop.jpg", "image/jpeg", "dummy".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/vintages")
                        .file(image)
                        .param("name", "테스트 매장")
                        .param("state", "서울")
                        .param("district", "마포구")
                        .param("detailAddr", "연남동 1-1")
                        .param("lat", "37.5")
                        .param("lon", "127.0"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(413))
                .andExpect(jsonPath("$.msg").value("이미지 용량이 너무 큽니다. 장당 10MB, 전체 100MB 이하로 올려주세요."));
    }
}
