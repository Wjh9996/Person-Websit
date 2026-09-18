package com.wjh.interviewbacked.dto;

/**
 * 触发分析后的返回：异步任务 id，前端据此轮询状态。
 */
public class AnalysisTriggerResult {
    private String taskId;

    public AnalysisTriggerResult() {
    }

    public AnalysisTriggerResult(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
