package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.dto.TagCount;
import com.wjh.interviewbacked.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper {

    Tag selectByUserIdAndName(@Param("userId") String userId, @Param("name") String name);

    int insert(Tag tag);

    /**
     * 标签云聚合。userId 非空统计该用户自己的笔记；为空（游客）只统计讨论广场公开笔记，
     * 不能跨用户裸统计，否则私有笔记的标签会泄露。
     */
    List<TagCount> selectTagCounts(@Param("userId") String userId);
}
