package com.wjh.interviewbacked.dto;

import lombok.Data;

/**
 * 新建简历请求体，对应前端 createResume(data, label)
 */
@Data
public class CreateResumeRequest {
    private ResumeData data;
    private String label;
}
