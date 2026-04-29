package com.timeremember.controller;

import com.timeremember.common.ApiResponse;
import com.timeremember.enums.StatisticsPeriod;
import com.timeremember.service.StatisticsService;
import com.timeremember.vo.statistics.CategoryCountVO;
import com.timeremember.vo.statistics.PeriodCountVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "统计")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Operation(summary = "按周/月/年统计事件数量")
    @GetMapping("/period")
    public ApiResponse<List<PeriodCountVO>> countByPeriod(
            @Parameter(description = "统计周期") @RequestParam(defaultValue = "MONTH") StatisticsPeriod period,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(statisticsService.countByPeriod(period, startDate, endDate));
    }

    @Operation(summary = "按分类统计事件数量")
    @GetMapping({"/category", "/categories"})
    public ApiResponse<List<CategoryCountVO>> countByCategory(
            @Parameter(description = "统计周期。未传自定义日期时生效") @RequestParam(defaultValue = "MONTH") StatisticsPeriod period,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(statisticsService.countByCategory(period, startDate, endDate));
    }
}
