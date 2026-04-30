package com.timeremember.service;

import com.timeremember.entity.EventAttachment;
import com.timeremember.vo.attachment.EventAttachmentVO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EventAttachmentService {

    EventAttachmentVO upload(Long eventId, MultipartFile file);

    List<EventAttachmentVO> listByEvent(Long eventId);

    EventAttachment getReadableAttachment(Long attachmentId);

    Resource loadContent(EventAttachment attachment);

    void delete(Long eventId, Long attachmentId);
}
