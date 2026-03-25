package com.vintly.infra.config.swagger.api;

import com.vintly.interfaces.board.BoardRequest;
import com.vintly.interfaces.presentation.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Board", description = "게시판 관련 API")
public interface SwaggerBoardApi {

    @Operation(summary = "게시글 목록 조회", description = "게시글 목록을 페이지네이션으로 조회합니다. keyword로 제목/내용 검색 가능합니다.")
    ApiResponse<?> getBoardList(
            @Parameter(description = "검색 키워드 (제목/내용)") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "게시글 상세 조회", description = "게시글 1개를 상세 조회합니다. 조회 시 viewCount가 1 증가합니다.")
    ApiResponse<?> getBoard(@PathVariable Long id);

    @Operation(summary = "게시글 생성", description = "게시글을 작성합니다.", security = @SecurityRequirement(name = "access"))
    ApiResponse<?> createBoard(@Valid @ModelAttribute BoardRequest.CreateBoard request);

    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다. 작성자만 수정 가능합니다.", security = @SecurityRequirement(name = "access"))
    ApiResponse<?> updateBoard(@PathVariable Long id, @Valid @ModelAttribute BoardRequest.UpdateBoard request);

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. 작성자 또는 관리자만 삭제 가능합니다.", security = @SecurityRequirement(name = "access"))
    ApiResponse<?> deleteBoard(@PathVariable Long id);
}
