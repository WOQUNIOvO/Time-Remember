package com.timeremember.vo.attachment;

import com.timeremember.enums.AttachmentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "事件附件响应")
public record EventAttachmentVO(
        @Schema(description = "附件 ID") Long id,
        @Schema(description = "事件 ID") Long eventId,
        @Schema(description = "附件类型") AttachmentType type,
        @Schema(description = "原始文件名") String originalName,
        @Schema(description = "MIME 类型") String contentType,
        @Schema(description = "文件大小，单位字节") Long fileSize,
        @Schema(description = "鉴权访问地址") String url,
        @Schema(description = "创建时间") LocalDateTime createdAt
) {
}
