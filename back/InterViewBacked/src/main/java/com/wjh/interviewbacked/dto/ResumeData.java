package com.wjh.interviewbacked.dto;

import lombok.Data;

import java.util.List;

/**
 * 简历完整数据结构（嵌套），对应前端 ResumeData。
 * 数据库中以整份 JSON 字符串存储于 resume.content 列。
 */
@Data
public class ResumeData {
    private BasicInfo basicInfo;
    private Education education;
    private List<String> summary;
    private List<Campus> campus;
    private List<String> skills;
    private List<Internship> internships;
    private List<Project> projects;
    private String projectDutyTitle;

    @Data
    public static class BasicInfo {
        private String name;
        private String title;
        private String phone;
        private String email;
    }

    @Data
    public static class Education {
        private String school;
        private String major;
        private String period;
    }

    @Data
    public static class Campus {
        private String period;
        private String description;
    }

    @Data
    public static class Internship {
        private String company;
        private String position;
        private String period;
        private List<String> duties;
    }

    @Data
    public static class Project {
        private String name;
        private String techStack;
        private String description;
        private List<String> duties;
    }
}
