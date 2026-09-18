package com.wjh.interviewbacked.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 回答所引用的笔记（用于溯源展示） */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRef {
    private String noteId;
    private String title;
    private Double score;
}
