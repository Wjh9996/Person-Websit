package com.wjh.interviewbacked.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记元数据实体，对应数据库 note 表（不含正文，正文在 note_content）。
 * tags 为 JSON 字符串数组（展示冗余；管理真相源见 note_tag_rel）。
 */
@Data
public class Note {
    private String id;
    /** 数据归属（消除水平越权） */
    private String userId;
    private String title;
    private String summary;
    /** 分类 id：frontend / backend / testing / tools / interview */
    private String category;
    /** 标签 JSON 数组（冗余展示） */
    private String tags;
    private Boolean pinned;
    private Integer views;
    /** 乐观锁版本号，防并发覆盖 */
    private Integer version;
    /** 正文 MD5，后端去重（拦截重复写库） */
    private String contentHash;
    /** 软删除：0 正常 1 回收站 */
    private Integer deleted;
    /** 软删除时间，用于 30 天自动清理 */
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
