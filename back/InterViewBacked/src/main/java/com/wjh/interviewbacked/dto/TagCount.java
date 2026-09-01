package com.wjh.interviewbacked.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签聚合项，对应前端 fetchAllTags() 返回 { tag, count }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagCount {
    private String tag;
    private Integer count;
}
