package com.vintly.interfaces.vintage;

import com.vintly.application.vintage.VintageFacade;
import com.vintly.domain.vintage.service.VintageService;
import com.vintly.infra.config.swagger.api.SwaggerVintageApi;
import com.vintly.interfaces.presentation.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vintages")
@Validated
public class VintageController implements SwaggerVintageApi {

    private final VintageService vintageService;
    private final VintageFacade vintageFacade;

    @Autowired
    public VintageController(VintageService vintageService, VintageFacade vintageFacade) {
        this.vintageService = vintageService;
        this.vintageFacade = vintageFacade;
    }

    // 빈티지 매장 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> createVintage(@ModelAttribute VintageRequest.CreateVintage createVintage){
        vintageFacade.createVintage(createVintage);

        return new ApiResponse<>(true, 200, "빈티지 매장 등록에 성공하였습니다.", null);
    }

    // 빈티지 매장 전체 조회
    @GetMapping()
    public ApiResponse<?> getVintageList(){

        return new ApiResponse<>(true, 200, "", vintageService.getVintageList());
    }

    // 빈티지 매장 상세 조회
    @GetMapping("/{id}")
    public ApiResponse<?> getVintage(@PathVariable Long id) {

        return new ApiResponse<>(true, 200, "", vintageFacade.getVintage(id));
    }

    // 빈티지 매장 수정 (PATCH)
    @PatchMapping("/{id}")
    public ApiResponse<?> updateVintage(@PathVariable Long id, @ModelAttribute VintageRequest.UpdateVintage request) {

        vintageFacade.updateVintage(id, request);
        return new ApiResponse<>(true, 200, "", "");
    }

    // 빈티지 매장 삭제
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteVintage(@PathVariable Long id) {
        vintageFacade.deleteVintage(id);
        return new ApiResponse<>(true, 200, "", "");
    }

    // 빈티지 매장 지역으로 검색 ex) 시(도)/시(구)
    @GetMapping("/search")
    public ApiResponse<?> searchByLocation(@RequestParam String state, @RequestParam String district) {

        return null;
    }

    // 위치 기반 검색 (추후) /api/vintages/nearby?lat=37.5&lon=127.0&radius=3 (km 단위)
}
