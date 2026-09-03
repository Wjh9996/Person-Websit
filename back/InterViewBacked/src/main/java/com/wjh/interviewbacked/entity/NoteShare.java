package com.wjh.interviewbacked.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记分享只读链接实体，对应数据库 note_share 表（阶段二功能，本次仅建表对齐）。
 * 他人凭 token 只读访问单篇，绕过 user_id 归属校验（独立鉴权）。
 */
@Data
public class NoteShare {
    private String id;
    private String noteId;
    private String token;
    private String createdBy;
    /** 可空 = 永久有效 */
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
