package com.timeremember.controller;

import com.timeremember.common.ApiResponse;
import com.timeremember.dto.event.EventCreateDTO;
import com.timeremember.dto.event.EventUpdateDTO;
import com.timeremember.service.EventRecordService;
import com.timeremember.vo.event.EventRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "事件记录")
@RestController
@RequestMapping("/events")
public class EventRecordController {

    private final EventRecordService eventRecordService;

    public EventRecordController(EventRecordService eventRecordService) {
        this.eventRecordService = eventRecordService;
    }

    @Operation(summary = "创建事件")
    @PostMapping
    public ApiResponse<EventRecordVO> create(@Valid @RequestBody EventCreateDTO dto) {
        return ApiResponse.success(eventRecordService.create(dto));
    }

    @Operation(summary = "查询我的事件")
    @GetMapping
    public ApiResponse<List<EventRecordVO>> listMine(
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(eventRecordService.listMine(startDate, endDate));
    }

    @Operation(summary = "更新事件")
    @PutMapping("/{id}")
    public ApiResponse<EventRecordVO> update(@PathVariable Long id, @Valid @RequestBody EventUpdateDTO dto) {
        return ApiResponse.success(eventRecordService.update(id, dto));
    }

    @Operation(summary = "删除事件")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        eventRecordService.delete(id);
        return ApiResponse.success();
    }
}
