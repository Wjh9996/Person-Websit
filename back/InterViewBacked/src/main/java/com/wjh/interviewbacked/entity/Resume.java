package com.wjh.interviewbacked.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历实体，对应数据库 resume 表。
 * content 为整份 ResumeData 的 JSON 字符串（嵌套结构不固定，整存最省心）。
 * 本次仅补齐安全字段 user_id / version / deleted，功能不重构。
 */
@Data
public class Resume {
    private String id;
    /** 数据归属（消除水平越权） */
    private String userId;
    /** 导航标题，通常取 basicInfo.title 或 name */
    private String label;
    /** 路由路径，如 /resume/custom-xxx */
    private String path;
    private String icon;
    /** ResumeData 的 JSON 字符串 */
    private String content;
    /** 乐观锁版本号 */
    private Integer version;
    /** 软删除：0 正常 1 已删 */
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
