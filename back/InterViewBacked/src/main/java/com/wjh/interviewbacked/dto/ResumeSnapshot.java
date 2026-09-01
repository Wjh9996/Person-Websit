package com.wjh.interviewbacked.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 简历快照，对应前端 fetchResumeSnapshot() 返回结构
 */
@Data
public class ResumeSnapshot {
    /** key = 简历 id，value = 简历完整内容 */
    private Map<String, ResumeData> resumes;
    private List<ResumeNavItem> navItems;
}
