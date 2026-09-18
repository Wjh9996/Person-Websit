package com.wjh.interviewbacked.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 提问结果：答案 + 引用来源 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AskResult {
    private String messageId;
    private String answer;
    private List<ChatRef> refs;
    /** true 表示知识库里没召回任何笔记，答案来自通用常识（会在前端提示） */
    private boolean fromKnowledgeBase;
}
