package com.wjh.interviewbacked.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历实体，对应数据库 resume 表。
 * content 为整份 ResumeData 的 JSON 字符串（嵌套结构不固定，整存最省心）。
 */
@Data
public class Resume {
    private String id;
    /** 导航标题，通常取 basicInfo.title 或 name */
    private String label;
    /** 路由路径，如 /resume/custom-xxx */
    private String path;
    private String icon;
    /** ResumeData 的 JSON 字符串 */
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
