package com.vintly.domain.vintage.dto;

import java.math.BigDecimal;
import java.util.List;

public class VintageInfo {

    public record Vintage(
            Long vintageId,
            String name,
            String state,
            String district,
            String detailAddr,
            BigDecimal lat,
            BigDecimal lon,
            String thumbnailPath
    ){}

    public record VintageDetail(
            Long vintageId,
            String name,
            String state,
            String district,
            String detailAddr,
            BigDecimal lat,
            BigDecimal lon,
            List<String> imagePaths
    ) {}
}
