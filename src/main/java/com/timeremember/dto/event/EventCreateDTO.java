package com.timeremember.dto.event;

import com.timeremember.enums.EventLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "创建事件请求")
public class EventCreateDTO {

    @NotNull
    @Schema(description = "事件日期", example = "2026-04-29")
    private LocalDate eventDate;

    @NotBlank
    @Size(max = 64)
    @Schema(description = "标题", example = "跑步 5 公里")
    private String title;

    @NotNull
    @Schema(description = "分类 ID")
    private Long categoryId;

    @Size(max = 512)
    @Schema(description = "备注")
    private String note;

    @NotNull
    @Schema(description = "重要程度", example = "MEDIUM")
    private EventLevel level;
}
