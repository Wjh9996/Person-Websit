package com.wjh.interviewbacked.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 提问入参 */
@Data
public class AskRequest {
    @NotBlank(message = "问题不能为空")
    private String question;
}
