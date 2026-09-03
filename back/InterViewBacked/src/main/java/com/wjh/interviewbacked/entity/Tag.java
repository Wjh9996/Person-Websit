package com.wjh.interviewbacked.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签字典实体，对应数据库 tag 表（用户维度，支持重命名/合并/删除）。
 */
@Data
public class Tag {
    private String id;
    private String userId;
    private String name;
    private String color;
    private LocalDateTime createdAt;
}
