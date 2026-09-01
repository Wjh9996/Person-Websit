package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.Resume;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResumeMapper {

    List<Resume> selectAll();

    Resume selectById(@Param("id") String id);

    int insert(Resume resume);

    int update(Resume resume);

    int deleteById(@Param("id") String id);
}
