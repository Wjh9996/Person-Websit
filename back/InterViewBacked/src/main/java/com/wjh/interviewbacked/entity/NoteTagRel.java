package com.wjh.interviewbacked.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 笔记-标签关联实体，对应数据库 note_tag_rel 表（多对多）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteTagRel {
    private String noteId;
    private String tagId;
}
