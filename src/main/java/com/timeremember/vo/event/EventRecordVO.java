package com.timeremember.vo.event;

import com.timeremember.enums.EventLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "事件响应")
public record EventRecordVO(
        @Schema(description = "事件 ID") Long id,
        @Schema(description = "事件日期") LocalDate eventDate,
        @Schema(description = "标题") String title,
        @Schema(description = "分类 ID") Long categoryId,
        @Schema(description = "分类名称") String categoryName,
        @Schema(description = "分类颜色") String categoryColor,
        @Schema(description = "备注") String note,
        @Schema(description = "重要程度") EventLevel level,
        @Schema(description = "创建时间") LocalDateTime createdAt,
        @Schema(description = "更新时间") LocalDateTime updatedAt
) {
}
