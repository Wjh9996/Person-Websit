package com.wjh.interviewbacked.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** AI 助手消息：role = user / assistant，assistant 消息的 refs 记录引用的笔记 */
@Data
public class ChatMessage {
    private String id;
    private String sessionId;
    private String userId;
    /** user / assistant */
    private String role;
    private String content;
    /** 引用笔记 JSON：[{noteId,title,score}] */
    private String refs;
    private LocalDateTime createdAt;
}
