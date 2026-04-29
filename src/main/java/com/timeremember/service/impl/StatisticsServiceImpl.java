package com.timeremember.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.timeremember.common.ErrorCode;
import com.timeremember.entity.EventRecord;
import com.timeremember.enums.StatisticsPeriod;
import com.timeremember.exception.BusinessException;
import com.timeremember.mapper.EventRecordMapper;
import com.timeremember.security.SecurityUtils;
import com.timeremember.service.StatisticsService;
import com.timeremember.vo.statistics.CategoryCountVO;
import com.timeremember.vo.statistics.PeriodCountVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final EventRecordMapper eventRecordMapper;

    public StatisticsServiceImpl(EventRecordMapper eventRecordMapper) {
        this.eventRecordMapper = eventRecordMapper;
    }

    @Override
    public List<PeriodCountVO> countByPeriod(StatisticsPeriod period, LocalDate startDate, LocalDate endDate) {
        Long userId = SecurityUtils.currentUserId();
        DateRange dateRange = resolveDateRange(period, startDate, endDate);
        LambdaQueryWrapper<EventRecord> wrapper = new LambdaQueryWrapper<EventRecord>()
                .eq(EventRecord::getUserId, userId)
                .orderByAsc(EventRecord::getEventDate);
        wrapper.ge(EventRecord::getEventDate, dateRange.startDate());
        wrapper.le(EventRecord::getEventDate, dateRange.endDate());
        Map<String, Long> grouped = eventRecordMapper.selectList(wrapper).stream()
                .collect(Collectors.groupingBy(item -> periodLabel(period, item.getEventDate()),
                        LinkedHashMap::new, Collectors.counting()));
        return grouped.entrySet().stream()
                .map(entry -> new PeriodCountVO(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(PeriodCountVO::period))
                .toList();
    }

    @Override
    public List<CategoryCountVO> countByCategory(StatisticsPeriod period, LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveDateRange(period, startDate, endDate);
        return eventRecordMapper.countByCategory(SecurityUtils.currentUserId(), dateRange.startDate(), dateRange.endDate());
    }

    private String periodLabel(StatisticsPeriod period, LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.CHINA);
        return switch (period) {
            case WEEK -> date.get(weekFields.weekBasedYear()) + "-W" + String.format("%02d", date.get(weekFields.weekOfWeekBasedYear()));
            case MONTH -> date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            case YEAR -> String.valueOf(date.getYear());
        };
    }

    private DateRange resolveDateRange(StatisticsPeriod period, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "开始日期不能晚于结束日期");
            }
            return new DateRange(startDate, endDate);
        }
        if (startDate != null) {
            return new DateRange(startDate, LocalDate.now());
        }
        if (endDate != null) {
            return new DateRange(LocalDate.of(1970, 1, 1), endDate);
        }

        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.CHINA);
        return switch (period) {
            case WEEK -> new DateRange(
                    today.with(weekFields.dayOfWeek(), 1),
                    today.with(weekFields.dayOfWeek(), 7)
            );
            case MONTH -> new DateRange(
                    today.with(TemporalAdjusters.firstDayOfMonth()),
                    today.with(TemporalAdjusters.lastDayOfMonth())
            );
            case YEAR -> new DateRange(
                    today.with(TemporalAdjusters.firstDayOfYear()),
                    today.with(TemporalAdjusters.lastDayOfYear())
            );
        };
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
