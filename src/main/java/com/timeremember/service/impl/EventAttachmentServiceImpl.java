package com.timeremember.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.timeremember.common.ErrorCode;
import com.timeremember.config.FileStorageProperties;
import com.timeremember.entity.EventAttachment;
import com.timeremember.entity.EventRecord;
import com.timeremember.enums.AttachmentType;
import com.timeremember.exception.BusinessException;
import com.timeremember.mapper.EventAttachmentMapper;
import com.timeremember.mapper.EventRecordMapper;
import com.timeremember.security.SecurityUtils;
import com.timeremember.service.EventAttachmentService;
import com.timeremember.vo.attachment.EventAttachmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
public class EventAttachmentServiceImpl implements EventAttachmentService {

    private static final int MAX_ATTACHMENTS_PER_EVENT = 9;

    private final EventAttachmentMapper attachmentMapper;
    private final EventRecordMapper eventRecordMapper;
    private final FileStorageProperties fileStorageProperties;
    private final Path uploadRoot;

    public EventAttachmentServiceImpl(EventAttachmentMapper attachmentMapper,
                                      EventRecordMapper eventRecordMapper,
                                      FileStorageProperties fileStorageProperties) {
        this.attachmentMapper = attachmentMapper;
        this.eventRecordMapper = eventRecordMapper;
        this.fileStorageProperties = fileStorageProperties;
        this.uploadRoot = Path.of(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventAttachmentVO upload(Long eventId, MultipartFile file) {
        Long userId = SecurityUtils.currentUserId();
        ensureEventMine(eventId, userId);
        validateFile(file);
        long count = attachmentMapper.selectCount(new LambdaQueryWrapper<EventAttachment>()
                .eq(EventAttachment::getUserId, userId)
                .eq(EventAttachment::getEventId, eventId));
        if (count >= MAX_ATTACHMENTS_PER_EVENT) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "单个事件最多上传 9 个附件");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "attachment" : file.getOriginalFilename());
        String extension = resolveExtension(originalName, file.getContentType());
        String storedName = UUID.randomUUID() + extension;
        Path eventDir = uploadRoot.resolve(userId.toString()).resolve(eventId.toString()).normalize();
        Path target = eventDir.resolve(storedName).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "非法文件路径");
        }

        try {
            Files.createDirectories(eventDir);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            log.error("Failed to store attachment, userId={}, eventId={}", userId, eventId, ex);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件保存失败");
        }

        EventAttachment attachment = new EventAttachment();
        attachment.setUserId(userId);
        attachment.setEventId(eventId);
        attachment.setType(resolveType(file.getContentType(), extension));
        attachment.setOriginalName(originalName);
        attachment.setStoredName(storedName);
        attachment.setContentType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setStoragePath(uploadRoot.relativize(target).toString().replace('\\', '/'));
        attachmentMapper.insert(attachment);
        log.info("Attachment uploaded, userId={}, eventId={}, attachmentId={}", userId, eventId, attachment.getId());
        return toVO(attachment);
    }

    @Override
    public List<EventAttachmentVO> listByEvent(Long eventId) {
        Long userId = SecurityUtils.currentUserId();
        ensureEventMine(eventId, userId);
        return attachmentMapper.selectList(new LambdaQueryWrapper<EventAttachment>()
                        .eq(EventAttachment::getUserId, userId)
                        .eq(EventAttachment::getEventId, eventId)
                        .orderByAsc(EventAttachment::getCreatedAt))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public EventAttachment getReadableAttachment(Long attachmentId) {
        Long userId = SecurityUtils.currentUserId();
        EventAttachment attachment = attachmentMapper.selectOne(new LambdaQueryWrapper<EventAttachment>()
                .eq(EventAttachment::getId, attachmentId)
                .eq(EventAttachment::getUserId, userId));
        if (attachment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "附件不存在");
        }
        return attachment;
    }

    @Override
    public Resource loadContent(EventAttachment attachment) {
        Path file = uploadRoot.resolve(attachment.getStoragePath()).normalize();
        if (!file.startsWith(uploadRoot) || !Files.exists(file)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "附件文件不存在");
        }
        try {
            return new UrlResource(file.toUri());
        } catch (MalformedURLException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "附件读取失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long eventId, Long attachmentId) {
        Long userId = SecurityUtils.currentUserId();
        ensureEventMine(eventId, userId);
        EventAttachment attachment = attachmentMapper.selectOne(new LambdaQueryWrapper<EventAttachment>()
                .eq(EventAttachment::getId, attachmentId)
                .eq(EventAttachment::getEventId, eventId)
                .eq(EventAttachment::getUserId, userId));
        if (attachment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "附件不存在");
        }
        attachmentMapper.deleteById(attachment.getId());
        deleteQuietly(uploadRoot.resolve(attachment.getStoragePath()).normalize());
        log.info("Attachment deleted, userId={}, eventId={}, attachmentId={}", userId, eventId, attachmentId);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要上传的文件");
        }
        if (file.getSize() > fileStorageProperties.getMaxFileSize()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件大小不能超过 50MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !fileStorageProperties.getAllowedContentTypes().contains(contentType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅支持 JPG、PNG、GIF、WEBP、MP4、WEBM、MOV 文件");
        }
    }

    private void ensureEventMine(Long eventId, Long userId) {
        boolean exists = eventRecordMapper.exists(new LambdaQueryWrapper<EventRecord>()
                .eq(EventRecord::getId, eventId)
                .eq(EventRecord::getUserId, userId));
        if (!exists) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "事件不存在");
        }
    }

    private AttachmentType resolveType(String contentType, String extension) {
        if ("image/gif".equals(contentType) || ".gif".equalsIgnoreCase(extension)) {
            return AttachmentType.GIF;
        }
        if (contentType != null && contentType.startsWith("video/")) {
            return AttachmentType.VIDEO;
        }
        return AttachmentType.IMAGE;
    }

    private String resolveExtension(String originalName, String contentType) {
        String extension = StringUtils.getFilenameExtension(originalName);
        if (StringUtils.hasText(extension)) {
            return "." + extension.toLowerCase(Locale.ROOT);
        }
        return switch (contentType == null ? "" : contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "video/mp4" -> ".mp4";
            case "video/webm" -> ".webm";
            case "video/quicktime" -> ".mov";
            default -> "";
        };
    }

    private EventAttachmentVO toVO(EventAttachment attachment) {
        return new EventAttachmentVO(
                attachment.getId(),
                attachment.getEventId(),
                attachment.getType(),
                attachment.getOriginalName(),
                attachment.getContentType(),
                attachment.getFileSize(),
                "/attachments/" + attachment.getId() + "/content",
                attachment.getCreatedAt()
        );
    }

    private void deleteQuietly(Path file) {
        try {
            if (file.startsWith(uploadRoot)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException ex) {
            log.warn("Failed to delete attachment file, path={}", file, ex);
        }
    }
}
