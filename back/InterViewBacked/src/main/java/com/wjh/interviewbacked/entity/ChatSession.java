package com.wjh.interviewbacked.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** AI 助手会话：归属用户，关联多条消息 */
@Data
public class ChatSession {
    private String id;
    private String userId;
    private String title;
    /** 软删除：0 正常 1 已删除 */
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
