package com.timeremember.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建分类请求")
public class CategoryCreateDTO {

    @NotBlank
    @Size(max = 32)
    @Schema(description = "分类名称", example = "健身")
    private String name;

    @NotBlank
    @Pattern(regexp = "^#[0-9a-fA-F]{6}$")
    @Schema(description = "颜色", example = "#409EFF")
    private String color;
}
