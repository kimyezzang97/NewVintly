package com.vintly.interfaces.youtube;

import com.vintly.application.youtube.YoutubeLinkFacade;
import com.vintly.infra.config.swagger.api.SwaggerYoutubeLinkApi;
import com.vintly.interfaces.presentation.ApiResponse;
import com.vintly.interfaces.presentation.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/youtube-links")
@RequiredArgsConstructor
public class YoutubeLinkController implements SwaggerYoutubeLinkApi {

    private final YoutubeLinkFacade youtubeLinkFacade;

    @GetMapping
    public ApiResponse<?> getYoutubeLinkList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return new ApiResponse<>(true, 200, "", PageResponse.from(youtubeLinkFacade.getYoutubeLinkList(pageable)));
    }
}
