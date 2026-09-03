package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.Resume;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResumeMapper {

    List<Resume> selectAll();

    /** 按归属用户查询（隔离多用户数据，排除软删除） */
    List<Resume> selectByUserId(@Param("userId") String userId);

    Resume selectById(@Param("id") String id);

    int insert(Resume resume);

    /** 保存覆盖（乐观锁）：WHERE id AND version */
    int updateWithVersion(Resume resume);

    int deleteById(@Param("id") String id);
}
