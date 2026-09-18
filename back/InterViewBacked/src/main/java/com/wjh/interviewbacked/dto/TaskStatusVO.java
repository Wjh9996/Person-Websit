package com.wjh.interviewbacked.dto;

/**
 * 异步任务状态（前端轮询用）。
 * status: pending / processing / success / failed
 */
public class TaskStatusVO {
    private String taskId;
    private String status;
    private String errorMsg;

    public TaskStatusVO() {
    }

    public TaskStatusVO(String taskId, String status, String errorMsg) {
        this.taskId = taskId;
        this.status = status;
        this.errorMsg = errorMsg;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
