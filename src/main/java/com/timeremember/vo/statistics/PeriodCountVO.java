package com.timeremember.vo.statistics;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "周期数量统计")
public record PeriodCountVO(
        @Schema(description = "周期标签") String period,
        @Schema(description = "数量") Long count
) {
}
