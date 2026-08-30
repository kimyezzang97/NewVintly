package com.vintly.infra.youtube;

import com.vintly.domain.youtube.dto.YoutubeLinkInfo;
import com.vintly.domain.youtube.repo.YoutubeLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class YoutubeLinkRepositoryImpl implements YoutubeLinkRepository {

    private final YoutubeLinkQueryDslRepository youtubeLinkQueryDslRepository;

    @Override
    public Page<YoutubeLinkInfo.YoutubeLinkSummary> findYoutubeLinkList(Pageable pageable) {
        return youtubeLinkQueryDslRepository.findYoutubeLinkList(pageable);
    }
}
