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
    /** 可见性：0 私有（仅作者本人可见） 1 讨论广场公开（所有可见） 2 链接分享（预留） */
    private Integer visibility;
    /** 软删除：0 正常 1 回收站 */
    private Integer deleted;
    /** 软删除时间，用于 30 天自动清理 */
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* ========== 以下为非数据库字段，仅用于联表查询回填 ========== */
    /** 作者 id（广场列表冗余，便于前端跳主页） */
    private String authorId;
    /** 作者昵称（广场列表 JOIN user.nickname 回填） */
    private String authorName;
    /** 全文检索相关度得分（仅知识库问答召回时回填） */
    private Double score;
}
