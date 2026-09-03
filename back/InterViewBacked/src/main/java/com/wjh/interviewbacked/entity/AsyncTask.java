package com.wjh.interviewbacked.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异步任务实体，对应数据库 async_task 表（MQ / 大模型 / 导出预留）。
 * payload / result 以 JSON 字符串存储；status: pending/processing/success/failed。
 */
@Data
public class AsyncTask {
    private String id;
    private String userId;
    /** 任务类型：ANALYSIS / EXPORT / ... */
    private String type;
    /** 关联业务主键（如 note_id） */
    private String bizId;
    private String status;
    private String payload;
    private String result;
    private String errorMsg;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
