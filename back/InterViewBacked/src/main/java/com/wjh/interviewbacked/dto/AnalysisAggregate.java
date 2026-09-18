package com.wjh.interviewbacked.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记分析结果聚合（按 analysis_type 拼装），供前端分析面板直接展示。
 * 任一类型缺失则为 null，前端按需渲染。
 */
public class AnalysisAggregate {
    private String summary;
    private List<String> keywords;
    private String category;
    private String categoryReason;
    private List<String> knowledge;
    private LocalDateTime analyzedAt;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryReason() {
        return categoryReason;
    }

    public void setCategoryReason(String categoryReason) {
        this.categoryReason = categoryReason;
    }

    public List<String> getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(List<String> knowledge) {
        this.knowledge = knowledge;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}
