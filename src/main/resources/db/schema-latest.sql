CREATE DATABASE IF NOT EXISTS time_remember
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE time_remember;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS event_record;
DROP TABLE IF EXISTS event_category;
DROP TABLE IF EXISTS sys_user;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sys_user (
    id BIGINT NOT NULL COMMENT '主键 ID，MyBatis-Plus ASSIGN_ID',
    username VARCHAR(32) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密密码',
    nickname VARCHAR(32) NULL COMMENT '昵称',
    role VARCHAR(16) NOT NULL DEFAULT 'USER' COMMENT '角色：USER / ADMIN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username_deleted (username, deleted),
    KEY idx_user_deleted (deleted)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='系统用户表';

CREATE TABLE event_category (
    id BIGINT NOT NULL COMMENT '主键 ID，MyBatis-Plus ASSIGN_ID',
    user_id BIGINT NOT NULL COMMENT '所属用户 ID',
    name VARCHAR(32) NOT NULL COMMENT '分类名称',
    color VARCHAR(16) NOT NULL COMMENT '分类颜色，如 #409EFF',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_user_name_deleted (user_id, name, deleted),
    KEY idx_category_user (user_id, deleted),
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='事件分类表';

CREATE TABLE event_record (
    id BIGINT NOT NULL COMMENT '主键 ID，MyBatis-Plus ASSIGN_ID',
    user_id BIGINT NOT NULL COMMENT '所属用户 ID',
    category_id BIGINT NOT NULL COMMENT '分类 ID',
    event_date DATE NOT NULL COMMENT '事件日期',
    title VARCHAR(64) NOT NULL COMMENT '事件标题',
    note VARCHAR(512) NULL COMMENT '备注',
    level VARCHAR(16) NOT NULL DEFAULT 'MEDIUM' COMMENT '重要程度：LOW / MEDIUM / HIGH',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
    PRIMARY KEY (id),
    KEY idx_event_user_date (user_id, event_date, deleted),
    KEY idx_event_category (category_id, deleted),
    KEY idx_event_user_category_date (user_id, category_id, event_date, deleted),
    CONSTRAINT fk_event_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_event_category FOREIGN KEY (category_id) REFERENCES event_category (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='事件记录表';
