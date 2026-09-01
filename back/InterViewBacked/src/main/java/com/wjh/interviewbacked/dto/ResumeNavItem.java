package com.wjh.interviewbacked.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简历导航项，对应前端 ResumeNavItem
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeNavItem {
    private String id;
    private String label;
    private String path;
    private String icon;
}
