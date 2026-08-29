package com.vintly.infra.youtube;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vintly.domain.youtube.dto.YoutubeLinkInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.vintly.domain.youtube.entity.QYoutubeLink.youtubeLink;

@Repository
@RequiredArgsConstructor
public class YoutubeLinkQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public Page<YoutubeLinkInfo.YoutubeLinkSummary> findYoutubeLinkList(Pageable pageable) {
        List<YoutubeLinkInfo.YoutubeLinkSummary> content = queryFactory
                .select(Projections.constructor(
                        YoutubeLinkInfo.YoutubeLinkSummary.class,
                        youtubeLink.youtubeLinkId,
                        youtubeLink.url,
                        youtubeLink.title,
                        youtubeLink.description,
                        youtubeLink.isAd,
                        youtubeLink.createdAt,
                        youtubeLink.updatedAt
                ))
                .from(youtubeLink)
                .orderBy(youtubeLink.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(youtubeLink.count())
                .from(youtubeLink);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
