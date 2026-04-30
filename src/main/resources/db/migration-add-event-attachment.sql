USE time_remember;

CREATE TABLE IF NOT EXISTS event_attachment (
    id BIGINT NOT NULL COMMENT '主键 ID，MyBatis-Plus ASSIGN_ID',
    user_id BIGINT NOT NULL COMMENT '所属用户 ID',
    event_id BIGINT NOT NULL COMMENT '事件 ID',
    type VARCHAR(16) NOT NULL COMMENT '附件类型：IMAGE / GIF / VIDEO',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name VARCHAR(128) NOT NULL COMMENT '存储文件名',
    content_type VARCHAR(100) NOT NULL COMMENT 'MIME 类型',
    file_size BIGINT NOT NULL COMMENT '文件大小，单位字节',
    storage_path VARCHAR(512) NOT NULL COMMENT '相对存储路径',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
    PRIMARY KEY (id),
    KEY idx_attachment_event (event_id, deleted),
    KEY idx_attachment_user_event (user_id, event_id, deleted),
    CONSTRAINT fk_attachment_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_attachment_event FOREIGN KEY (event_id) REFERENCES event_record (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='事件附件表';
