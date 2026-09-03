package com.wjh.interviewbacked.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件/附件元信息实体，对应数据库 file 表（对象存储预留）。
 * 图片禁止 base64 存正文，必须走对象存储 key（object_key）+ 访问 url。
 */
@Data
public class File {
    private String id;
    private String userId;
    private String noteId;
    private String bucket;
    /** 对象存储 key */
    private String objectKey;
    /** 访问 URL */
    private String url;
    private String name;
    private String ext;
    private Long size;
    private String mime;
    private LocalDateTime createdAt;
}
