package com.vintly.domain.youtube.repo;

import com.vintly.domain.youtube.dto.YoutubeLinkInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface YoutubeLinkRepository {

    Page<YoutubeLinkInfo.YoutubeLinkSummary> findYoutubeLinkList(Pageable pageable);
}
