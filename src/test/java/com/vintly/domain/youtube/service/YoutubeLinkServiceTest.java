package com.vintly.domain.youtube.service;

import com.vintly.domain.youtube.dto.YoutubeLinkInfo;
import com.vintly.domain.youtube.repo.YoutubeLinkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YoutubeLinkServiceTest {

    @Mock
    private YoutubeLinkRepository youtubeLinkRepository;

    @InjectMocks
    private YoutubeLinkService youtubeLinkService;

    @Test
    @DisplayName("페이지 요청을 받으면 리포지토리에서 조회한 유튜브 링크 목록을 그대로 반환한다.")
    void shouldReturnYoutubeLinkListFromRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        YoutubeLinkInfo.YoutubeLinkSummary summary = new YoutubeLinkInfo.YoutubeLinkSummary(
                1L, "https://youtu.be/abc", "빈티지 소개 영상", "설명", false,
                LocalDateTime.now(), LocalDateTime.now()
        );
        Page<YoutubeLinkInfo.YoutubeLinkSummary> page = new PageImpl<>(List.of(summary), pageable, 1);
        when(youtubeLinkRepository.findYoutubeLinkList(pageable)).thenReturn(page);

        // Act
        Page<YoutubeLinkInfo.YoutubeLinkSummary> result = youtubeLinkService.getYoutubeLinkList(pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("빈티지 소개 영상");
        verify(youtubeLinkRepository).findYoutubeLinkList(pageable);
    }
}
