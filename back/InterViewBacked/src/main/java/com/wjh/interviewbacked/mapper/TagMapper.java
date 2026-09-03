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

    /** 全站标签云聚合（跨用户，公开） */
    List<TagCount> selectTagCounts();
}
