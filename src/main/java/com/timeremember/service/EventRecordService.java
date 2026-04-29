package com.timeremember.service;

import com.timeremember.dto.event.EventCreateDTO;
import com.timeremember.dto.event.EventUpdateDTO;
import com.timeremember.vo.event.EventRecordVO;

import java.time.LocalDate;
import java.util.List;

public interface EventRecordService {

    EventRecordVO create(EventCreateDTO dto);

    List<EventRecordVO> listMine(LocalDate startDate, LocalDate endDate);

    EventRecordVO update(Long id, EventUpdateDTO dto);

    void delete(Long id);
}
