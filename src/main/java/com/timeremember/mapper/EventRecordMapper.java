package com.timeremember.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timeremember.entity.EventRecord;
import com.timeremember.vo.event.EventRecordVO;
import com.timeremember.vo.statistics.CategoryCountVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface EventRecordMapper extends BaseMapper<EventRecord> {

    List<EventRecordVO> selectUserEvents(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    List<CategoryCountVO> countByCategory(@Param("userId") Long userId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
}
