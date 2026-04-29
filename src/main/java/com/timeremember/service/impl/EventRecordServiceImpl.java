package com.timeremember.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.timeremember.common.ErrorCode;
import com.timeremember.dto.event.EventCreateDTO;
import com.timeremember.dto.event.EventUpdateDTO;
import com.timeremember.entity.EventCategory;
import com.timeremember.entity.EventRecord;
import com.timeremember.exception.BusinessException;
import com.timeremember.mapper.EventCategoryMapper;
import com.timeremember.mapper.EventRecordMapper;
import com.timeremember.security.SecurityUtils;
import com.timeremember.service.EventRecordService;
import com.timeremember.vo.event.EventRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class EventRecordServiceImpl implements EventRecordService {

    private final EventRecordMapper eventRecordMapper;
    private final EventCategoryMapper categoryMapper;

    public EventRecordServiceImpl(EventRecordMapper eventRecordMapper, EventCategoryMapper categoryMapper) {
        this.eventRecordMapper = eventRecordMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventRecordVO create(EventCreateDTO dto) {
        Long userId = SecurityUtils.currentUserId();
        ensureCategoryMine(dto.getCategoryId(), userId);
        EventRecord record = new EventRecord();
        record.setUserId(userId);
        record.setCategoryId(dto.getCategoryId());
        record.setEventDate(dto.getEventDate());
        record.setTitle(dto.getTitle());
        record.setNote(dto.getNote());
        record.setLevel(dto.getLevel());
        eventRecordMapper.insert(record);
        log.info("Event created, userId={}, eventId={}", userId, record.getId());
        return eventRecordMapper.selectUserEvents(userId, record.getEventDate(), record.getEventDate()).stream()
                .filter(item -> item.id().equals(record.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "事件不存在"));
    }

    @Override
    public List<EventRecordVO> listMine(LocalDate startDate, LocalDate endDate) {
        Long userId = SecurityUtils.currentUserId();
        return eventRecordMapper.selectUserEvents(userId, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventRecordVO update(Long id, EventUpdateDTO dto) {
        Long userId = SecurityUtils.currentUserId();
        EventRecord record = getMine(id, userId);
        ensureCategoryMine(dto.getCategoryId(), userId);
        record.setCategoryId(dto.getCategoryId());
        record.setEventDate(dto.getEventDate());
        record.setTitle(dto.getTitle());
        record.setNote(dto.getNote());
        record.setLevel(dto.getLevel());
        eventRecordMapper.updateById(record);
        return eventRecordMapper.selectUserEvents(userId, record.getEventDate(), record.getEventDate()).stream()
                .filter(item -> item.id().equals(record.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "事件不存在"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long userId = SecurityUtils.currentUserId();
        EventRecord record = getMine(id, userId);
        eventRecordMapper.deleteById(record.getId());
        log.info("Event deleted, userId={}, eventId={}", userId, id);
    }

    private EventRecord getMine(Long id, Long userId) {
        EventRecord record = eventRecordMapper.selectOne(new LambdaQueryWrapper<EventRecord>()
                .eq(EventRecord::getId, id)
                .eq(EventRecord::getUserId, userId));
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "事件不存在");
        }
        return record;
    }

    private void ensureCategoryMine(Long categoryId, Long userId) {
        boolean exists = categoryMapper.exists(new LambdaQueryWrapper<EventCategory>()
                .eq(EventCategory::getId, categoryId)
                .eq(EventCategory::getUserId, userId));
        if (!exists) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }
    }
}
