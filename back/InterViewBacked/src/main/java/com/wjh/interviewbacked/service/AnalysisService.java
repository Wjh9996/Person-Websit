package com.wjh.interviewbacked.service;

import com.wjh.interviewbacked.dto.AnalysisAggregate;
import com.wjh.interviewbacked.dto.TaskStatusVO;

import java.util.List;

/**
 * 智能笔记分析服务：
 * - 触发分析（异步，返回任务 id）
 * - 聚合查询笔记有效分析结果
 * - 查询异步任务状态（前端轮询）
 * - 正文变更时使旧分析失效
 */
public interface AnalysisService {

    /** 触发分析，返回异步任务 id；types 为空则全量（summary/keywords/category/knowledge） */
    String triggerAnalysis(String noteId, String userId, List<String> types);

    /** 聚合查询某笔记全部有效分析结果 */
    AnalysisAggregate getAnalysis(String noteId, String userId);

    /** 查询异步任务状态 */
    TaskStatusVO getTaskStatus(String taskId, String userId);

    /** 正文变更时使旧分析失效（供 NoteServiceImpl 调用） */
    void invalidateByNote(String noteId);
}
