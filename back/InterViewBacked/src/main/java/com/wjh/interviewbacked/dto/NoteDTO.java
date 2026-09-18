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
    /** 可见性：0 私有（仅本人） 1 讨论广场公开 */
    private Integer visibility;
    /** 作者 id / 昵称：广场必填，"我的笔记"为本人，便于前端统一渲染 */
    private String authorId;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
