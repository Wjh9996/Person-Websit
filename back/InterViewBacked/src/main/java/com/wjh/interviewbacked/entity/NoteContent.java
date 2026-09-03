package com.wjh.interviewbacked.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 笔记正文实体，对应数据库 note_content 表（垂直拆分，仅在详情时按主键单查）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteContent {
    private String noteId;
    /** Markdown 正文（大字段） */
    private String content;
}
