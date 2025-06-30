package com.vintly.interfaces.vintage;

import com.vintly.domain.vintage.service.VintageService;
import com.vintly.interfaces.presentation.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vintages")
@Validated
public class VintageController {

    private final VintageService vintageService;

    @Autowired
    public VintageController(VintageService vintageService) {
        this.vintageService = vintageService;
    }

    // 빈티지 매장 등록
    @PostMapping()
    public ApiResponse<?> createVintage(@ModelAttribute VintageRequest.CreateVintage createVintage){
        vintageService.createVintage(createVintage);

        return new ApiResponse<>(true, 200, "빈티지 매장 등록에 성공하였습니다.", null);
    }

    // 빈티지 매장 전체 조회
    @GetMapping()
    public ApiResponse<?> getAllVintages(){
        return null;
    }

    // 빈티지 매장 단건 조회
    @GetMapping("/{id}")
    public ApiResponse<?> getVintage(@PathVariable Long id) {
        return null;
    }

    // 빈티지 매장 수정 (PATCH)
    @PatchMapping("/{id}")
    public ApiResponse<?> updateVintage(@PathVariable Long id, @RequestBody VintageRequest request) {
        return null;
    }

    // 빈티지 매장 삭제
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteVintage(@PathVariable Long id) {
        return null;
    }

    // 빈티지 매장 지역으로 검색 ex) 시(도)/시(구)
    @GetMapping("/search")
    public ApiResponse<?> searchByLocation(@RequestParam String state, @RequestParam String district) {
        return null;
    }

    // 위치 기반 검색 (추후) /api/vintages/nearby?lat=37.5&lon=127.0&radius=3 (km 단위)
}
