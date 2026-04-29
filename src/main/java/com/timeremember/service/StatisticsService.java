package com.timeremember.service;

import com.timeremember.enums.StatisticsPeriod;
import com.timeremember.vo.statistics.CategoryCountVO;
import com.timeremember.vo.statistics.PeriodCountVO;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {

    List<PeriodCountVO> countByPeriod(StatisticsPeriod period, LocalDate startDate, LocalDate endDate);

    List<CategoryCountVO> countByCategory(StatisticsPeriod period, LocalDate startDate, LocalDate endDate);
}
