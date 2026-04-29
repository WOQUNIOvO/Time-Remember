package com.timeremember.vo.statistics;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "分类数量统计")
public record CategoryCountVO(
        @Schema(description = "分类 ID") Long categoryId,
        @Schema(description = "分类名称") String categoryName,
        @Schema(description = "分类颜色") String categoryColor,
        @Schema(description = "数量") Long count
) {
}
