package com.timeremember.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.timeremember.enums.AttachmentType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("event_attachment")
public class EventAttachment {

    private Long id;

    private Long userId;

    private Long eventId;

    private AttachmentType type;

    private String originalName;

    private String storedName;

    private String contentType;

    private Long fileSize;

    private String storagePath;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
