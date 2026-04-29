package com.timeremember.controller;

import com.timeremember.common.ApiResponse;
import com.timeremember.dto.category.CategoryCreateDTO;
import com.timeremember.dto.category.CategoryUpdateDTO;
import com.timeremember.service.CategoryService;
import com.timeremember.vo.category.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "事件分类")
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public ApiResponse<CategoryVO> create(@Valid @RequestBody CategoryCreateDTO dto) {
        return ApiResponse.success(categoryService.create(dto));
    }

    @Operation(summary = "查询我的分类")
    @GetMapping
    public ApiResponse<List<CategoryVO>> listMine() {
        return ApiResponse.success(categoryService.listMine());
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public ApiResponse<CategoryVO> update(@PathVariable Long id, @Valid @RequestBody CategoryUpdateDTO dto) {
        return ApiResponse.success(categoryService.update(id, dto));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.success();
    }
}
