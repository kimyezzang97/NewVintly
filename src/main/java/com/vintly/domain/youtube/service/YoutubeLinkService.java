package com.vintly.domain.youtube.service;

import com.vintly.domain.youtube.dto.YoutubeLinkInfo;
import com.vintly.domain.youtube.repo.YoutubeLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class YoutubeLinkService {

    private final YoutubeLinkRepository youtubeLinkRepository;

    @Transactional(readOnly = true)
    public Page<YoutubeLinkInfo.YoutubeLinkSummary> getYoutubeLinkList(Pageable pageable) {
        return youtubeLinkRepository.findYoutubeLinkList(pageable);
    }
}
