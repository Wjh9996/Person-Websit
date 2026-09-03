package com.wjh.interviewbacked.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记分析实体，对应数据库 note_analysis 表（大模型结果预留）。
 * result 以 JSON 字符串存储，适配不同分析类型（keywords/summary/category/similar）。
 */
@Data
public class NoteAnalysis {
    private String id;
    private String noteId;
    private String userId;
    /** 分析类型：keywords / summary / category / similar */
    private String analysisType;
    /** 结构化分析结果（JSON 字符串） */
    private String result;
    /** 调用的模型标识（如 gpt-4o / 自建模型） */
    private String model;
    /** 1 有效 0 失效（内容大改后旧分析失效） */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
