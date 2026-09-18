package com.wjh.interviewbacked.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** 会话中的一条消息（assistant 消息带引用） */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVO {
    private String id;
    /** user / assistant */
    private String role;
    private String content;
    private List<ChatRef> refs;
    private LocalDateTime createdAt;
}
