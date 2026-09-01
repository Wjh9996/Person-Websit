package com.wjh.interviewbacked.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 新建 / 更新笔记入参（对应前端 NoteDraft）
 */
@Data
public class NoteDraft {
    @NotBlank(message = "标题不能为空")
    private String title;
    private String summary;
    private String content;
    @NotBlank(message = "分类不能为空")
    private String category;
    private List<String> tags;
    private Boolean pinned;
}
