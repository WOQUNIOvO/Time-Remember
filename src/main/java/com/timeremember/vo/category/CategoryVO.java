package com.timeremember.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "分类响应")
public record CategoryVO(
        @Schema(description = "分类 ID") Long id,
        @Schema(description = "名称") String name,
        @Schema(description = "颜色") String color,
        @Schema(description = "创建时间") LocalDateTime createdAt,
        @Schema(description = "更新时间") LocalDateTime updatedAt
) {
}
