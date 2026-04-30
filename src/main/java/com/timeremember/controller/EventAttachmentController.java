package com.timeremember.controller;

import com.timeremember.common.ApiResponse;
import com.timeremember.entity.EventAttachment;
import com.timeremember.service.EventAttachmentService;
import com.timeremember.vo.attachment.EventAttachmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Tag(name = "事件附件")
@RestController
@RequestMapping
public class EventAttachmentController {

    private final EventAttachmentService eventAttachmentService;

    public EventAttachmentController(EventAttachmentService eventAttachmentService) {
        this.eventAttachmentService = eventAttachmentService;
    }

    @Operation(summary = "上传事件附件")
    @PostMapping(value = "/events/{eventId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<EventAttachmentVO> upload(
            @PathVariable Long eventId,
            @Parameter(description = "图片、GIF 或视频文件") @RequestPart("file") MultipartFile file) {
        return ApiResponse.success(eventAttachmentService.upload(eventId, file));
    }

    @Operation(summary = "查询事件附件")
    @GetMapping("/events/{eventId}/attachments")
    public ApiResponse<List<EventAttachmentVO>> list(@PathVariable Long eventId) {
        return ApiResponse.success(eventAttachmentService.listByEvent(eventId));
    }

    @Operation(summary = "删除事件附件")
    @DeleteMapping("/events/{eventId}/attachments/{attachmentId}")
    public ApiResponse<Void> delete(@PathVariable Long eventId, @PathVariable Long attachmentId) {
        eventAttachmentService.delete(eventId, attachmentId);
        return ApiResponse.success();
    }

    @Operation(summary = "读取附件内容")
    @GetMapping("/attachments/{attachmentId}/content")
    public ResponseEntity<Resource> content(@PathVariable Long attachmentId) {
        EventAttachment attachment = eventAttachmentService.getReadableAttachment(attachmentId);
        Resource resource = eventAttachmentService.loadContent(attachment);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .contentLength(attachment.getFileSize())
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).cachePrivate())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(attachment.getOriginalName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(resource);
    }
}
