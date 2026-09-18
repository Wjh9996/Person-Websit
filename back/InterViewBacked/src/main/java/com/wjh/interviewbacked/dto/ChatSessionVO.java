package com.wjh.interviewbacked.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 会话列表项 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionVO {
    private String id;
    private String title;
    private LocalDateTime updatedAt;
}
