package com.vintly.interfaces.board;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class BoardRequest {

    public record CreateBoard(
            @Schema(description = "제목")
            @NotBlank(message = "제목을 입력해주세요.")
            @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
            String title,

            @Schema(description = "본문 내용")
            @NotBlank(message = "내용을 입력해주세요.")
            String content,

            @Schema(description = "이미지 파일 목록 (최대 10장)")
            @Size(max = 10, message = "이미지는 10장 이하만 업로드 가능합니다.")
            List<MultipartFile> images
    ) {}

    public record UpdateBoard(
            @Schema(description = "제목")
            @NotBlank(message = "제목을 입력해주세요.")
            @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
            String title,

            @Schema(description = "본문 내용")
            @NotBlank(message = "내용을 입력해주세요.")
            String content,

            @Schema(description = "유지할 이미지 ID 목록")
            List<Long> remainImgIdList,

            @Schema(description = "새로 업로드할 이미지 목록 (최대 10장)")
            @Size(max = 10, message = "이미지는 10장 이하만 업로드 가능합니다.")
            List<MultipartFile> imgList
    ) {}
}
