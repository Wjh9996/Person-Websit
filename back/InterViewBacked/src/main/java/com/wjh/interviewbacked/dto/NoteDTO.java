package com.wjh.interviewbacked.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记 API 模型（对外传输），tags 为 List<String>
 */
@Data
public class NoteDTO {
    private String id;
    private String title;
    private String summary;
    private String content;
    private String category;
    private List<String> tags;
    private Boolean pinned;
    private Integer views;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
