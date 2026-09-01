package com.wjh.interviewbacked.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记实体，对应数据库 note 表。
 * tags 以 JSON 字符串存储（如 ["Vue","TypeScript"]），与前端 List<String> 互转在 Service 层完成。
 */
@Data
public class Note {
    private String id;
    private String title;
    private String summary;
    /** Markdown 正文 */
    private String content;
    /** 分类 id：frontend / backend / testing / tools / interview */
    private String category;
    /** JSON 字符串数组 */
    private String tags;
    private Boolean pinned;
    private Integer views;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
