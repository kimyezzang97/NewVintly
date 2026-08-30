package com.vintly.application.youtube;

import com.vintly.domain.youtube.service.YoutubeLinkService;
import com.vintly.interfaces.youtube.YoutubeLinkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class YoutubeLinkFacade {

    private final YoutubeLinkService youtubeLinkService;

    public Page<YoutubeLinkResponse.YoutubeLinkList> getYoutubeLinkList(Pageable pageable) {
        return youtubeLinkService.getYoutubeLinkList(pageable)
                .map(YoutubeLinkResponse.YoutubeLinkList::from);
    }
}
