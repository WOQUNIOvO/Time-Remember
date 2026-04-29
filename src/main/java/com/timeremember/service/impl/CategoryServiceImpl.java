package com.timeremember.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.timeremember.common.ErrorCode;
import com.timeremember.dto.category.CategoryCreateDTO;
import com.timeremember.dto.category.CategoryUpdateDTO;
import com.timeremember.entity.EventCategory;
import com.timeremember.exception.BusinessException;
import com.timeremember.mapper.EventCategoryMapper;
import com.timeremember.security.SecurityUtils;
import com.timeremember.service.CategoryService;
import com.timeremember.vo.category.CategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    private final EventCategoryMapper categoryMapper;

    public CategoryServiceImpl(EventCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO create(CategoryCreateDTO dto) {
        Long userId = SecurityUtils.currentUserId();
        ensureNameAvailable(userId, dto.getName(), null);
        EventCategory category = new EventCategory();
        category.setUserId(userId);
        category.setName(dto.getName());
        category.setColor(dto.getColor());
        categoryMapper.insert(category);
        log.info("Category created, userId={}, categoryId={}", userId, category.getId());
        return toVO(category);
    }

    @Override
    public List<CategoryVO> listMine() {
        Long userId = SecurityUtils.currentUserId();
        return categoryMapper.selectList(new LambdaQueryWrapper<EventCategory>()
                        .eq(EventCategory::getUserId, userId)
                        .orderByDesc(EventCategory::getCreatedAt))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO update(Long id, CategoryUpdateDTO dto) {
        Long userId = SecurityUtils.currentUserId();
        EventCategory category = getMine(id, userId);
        ensureNameAvailable(userId, dto.getName(), id);
        category.setName(dto.getName());
        category.setColor(dto.getColor());
        categoryMapper.updateById(category);
        return toVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long userId = SecurityUtils.currentUserId();
        EventCategory category = getMine(id, userId);
        categoryMapper.deleteById(category.getId());
        log.info("Category deleted, userId={}, categoryId={}", userId, id);
    }

    private EventCategory getMine(Long id, Long userId) {
        EventCategory category = categoryMapper.selectOne(new LambdaQueryWrapper<EventCategory>()
                .eq(EventCategory::getId, id)
                .eq(EventCategory::getUserId, userId));
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }
        return category;
    }

    private void ensureNameAvailable(Long userId, String name, Long excludeId) {
        LambdaQueryWrapper<EventCategory> wrapper = new LambdaQueryWrapper<EventCategory>()
                .eq(EventCategory::getUserId, userId)
                .eq(EventCategory::getName, name);
        if (excludeId != null) {
            wrapper.ne(EventCategory::getId, excludeId);
        }
        if (categoryMapper.exists(wrapper)) {
            throw new BusinessException(ErrorCode.CONFLICT, "分类名称已存在");
        }
    }

    private CategoryVO toVO(EventCategory category) {
        return new CategoryVO(category.getId(), category.getName(), category.getColor(),
                category.getCreatedAt(), category.getUpdatedAt());
    }
}
