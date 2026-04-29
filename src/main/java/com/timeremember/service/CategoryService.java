package com.timeremember.service;

import com.timeremember.dto.category.CategoryCreateDTO;
import com.timeremember.dto.category.CategoryUpdateDTO;
import com.timeremember.vo.category.CategoryVO;

import java.util.List;

public interface CategoryService {

    CategoryVO create(CategoryCreateDTO dto);

    List<CategoryVO> listMine();

    CategoryVO update(Long id, CategoryUpdateDTO dto);

    void delete(Long id);
}
