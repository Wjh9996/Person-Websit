package com.wjh.interviewbacked.service.impl;

import com.wjh.interviewbacked.component.LlmClient;
import com.wjh.interviewbacked.common.LlmProperties;
import com.wjh.interviewbacked.dto.AnalysisAggregate;
import com.wjh.interviewbacked.dto.TaskStatusVO;
import com.wjh.interviewbacked.entity.AsyncTask;
import com.wjh.interviewbacked.entity.Note;
import com.wjh.interviewbacked.entity.NoteAnalysis;
import com.wjh.interviewbacked.exception.BusinessException;
import com.wjh.interviewbacked.mapper.AsyncTaskMapper;
import com.wjh.interviewbacked.mapper.NoteAnalysisMapper;
import com.wjh.interviewbacked.mapper.NoteContentMapper;
import com.wjh.interviewbacked.mapper.NoteMapper;
import com.wjh.interviewbacked.service.AnalysisService;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 智能笔记分析服务实现：
 * 触发即建 async_task 并由 TaskExecutor 异步执行（不阻塞笔记主流程），
 * 逐个分析类型调大模型、结果落 note_analysis，最后更新任务状态。
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final NoteMapper noteMapper;
    private final NoteContentMapper noteContentMapper;
    private final NoteAnalysisMapper noteAnalysisMapper;
    private final AsyncTaskMapper asyncTaskMapper;
    private final LlmClient llmClient;
    private final LlmProperties props;
    private final TaskExecutor taskExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisServiceImpl(NoteMapper noteMapper, NoteContentMapper noteContentMapper,
                               NoteAnalysisMapper noteAnalysisMapper, AsyncTaskMapper asyncTaskMapper,
                               LlmClient llmClient, LlmProperties props, TaskExecutor taskExecutor) {
        this.noteMapper = noteMapper;
        this.noteContentMapper = noteContentMapper;
        this.noteAnalysisMapper = noteAnalysisMapper;
        this.asyncTaskMapper = asyncTaskMapper;
        this.llmClient = llmClient;
        this.props = props;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public String triggerAnalysis(String noteId, String userId, List<String> types) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) throw new BusinessException("笔记不存在");
        if (!userId.equals(note.getUserId())) throw new BusinessException(403, "无权分析他人笔记");

        List<String> targetTypes = (types == null || types.isEmpty())
                ? List.of("summary", "keywords", "category", "knowledge")
                : types;

        String taskId = UUID.randomUUID().toString();
        AsyncTask task = new AsyncTask();
        task.setId(taskId);
        task.setUserId(userId);
        task.setType("ANALYSIS");
        task.setBizId(noteId);
        task.setStatus("pending");
        task.setRetryCount(0);
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        asyncTaskMapper.insert(task);

        List<String> finalTypes = targetTypes;
        taskExecutor.execute(() -> runAnalysis(taskId, noteId, userId, finalTypes));
        return taskId;
    }

    @Override
    public AnalysisAggregate getAnalysis(String noteId, String userId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) throw new BusinessException("笔记不存在");
        if (!userId.equals(note.getUserId())) throw new BusinessException(403, "无权查看他人笔记分析");

        List<NoteAnalysis> list = noteAnalysisMapper.selectByNote(noteId);
        AnalysisAggregate agg = new AnalysisAggregate();
        LocalDateTime latest = null;
        for (NoteAnalysis a : list) {
            if (a.getUpdatedAt() != null && (latest == null || a.getUpdatedAt().isAfter(latest))) {
                latest = a.getUpdatedAt();
            }
            parseResult(a, agg);
        }
        agg.setAnalyzedAt(latest);
        return agg;
    }

    @Override
    public TaskStatusVO getTaskStatus(String taskId, String userId) {
        AsyncTask task = asyncTaskMapper.selectById(taskId);
        if (task == null) throw new BusinessException("任务不存在");
        if (userId != null && !userId.equals(task.getUserId())) {
            throw new BusinessException(403, "无权查看该任务");
        }
        return new TaskStatusVO(taskId, task.getStatus(), task.getErrorMsg());
    }

    @Override
    public void invalidateByNote(String noteId) {
        noteAnalysisMapper.invalidateByNote(noteId);
    }

    /** 异步执行：逐个类型调大模型并落库，最后更新任务状态 */
    private void runAnalysis(String taskId, String noteId, String userId, List<String> types) {
        try {
            String content = noteContentMapper.selectContentByNoteId(noteId);
            Note note = noteMapper.selectById(noteId);
            String userText = "标题：" + (note != null ? note.getTitle() : "") + "\n正文：\n" + (content == null ? "" : content);
            LocalDateTime now = LocalDateTime.now();
            for (String type : types) {
                String resultJson = llmClient.analyze(systemPrompt(type), userText);
                NoteAnalysis a = new NoteAnalysis();
                a.setId(UUID.randomUUID().toString());
                a.setNoteId(noteId);
                a.setUserId(userId);
                a.setAnalysisType(type);
                a.setResult(resultJson);
                a.setModel(props.getModel());
                a.setStatus(1);
                a.setCreatedAt(now);
                a.setUpdatedAt(now);
                noteAnalysisMapper.insert(a);
            }
            asyncTaskMapper.updateStatus(taskId, "success", null, null, LocalDateTime.now());
        } catch (Exception e) {
            asyncTaskMapper.updateStatus(taskId, "failed", null, e.getMessage(), LocalDateTime.now());
        }
    }

    /** 解析单条分析结果 JSON，填入聚合对象对应字段 */
    @SuppressWarnings("unchecked")
    private void parseResult(NoteAnalysis a, AnalysisAggregate agg) {
        String json = a.getResult();
        if (json == null || json.isBlank()) return;
        try {
            Map<String, Object> node = objectMapper.readValue(json, Map.class);
            switch (a.getAnalysisType()) {
                case "summary":
                    if (node.get("summary") != null) agg.setSummary(String.valueOf(node.get("summary")));
                    break;
                case "keywords":
                    if (node.get("keywords") instanceof List<?> list) {
                        agg.setKeywords(list.stream().map(String::valueOf).toList());
                    }
                    break;
                case "category":
                    if (node.get("category") != null) agg.setCategory(String.valueOf(node.get("category")));
                    if (node.get("reason") != null) agg.setCategoryReason(String.valueOf(node.get("reason")));
                    break;
                case "knowledge":
                    if (node.get("points") instanceof List<?> list) {
                        agg.setKnowledge(list.stream().map(String::valueOf).toList());
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception ignored) {
            // 单条解析失败不影响其他类型展示
        }
    }

    private String systemPrompt(String type) {
        return switch (type) {
            case "summary" -> "你是笔记助手。请用2-3句话概括用户提供的笔记核心内容。仅输出JSON格式：{\"summary\":\"...\"}，不要输出其他任何内容。";
            case "keywords" -> "提取这份笔记的3-8个关键词。仅输出JSON：{\"keywords\":[\"...\"]}，不要输出其他内容。";
            case "category" -> "判断笔记最适合的分类，可选值：frontend,backend,testing,tools,interview。仅输出JSON：{\"category\":\"...\",\"reason\":\"...\"}，不要输出其他内容。";
            case "knowledge" -> "提炼笔记中的核心知识点或考点，3-8条。仅输出JSON：{\"points\":[\"...\"]}，不要输出其他内容。";
            default -> "提取笔记要点，仅输出JSON。";
        };
    }
}
