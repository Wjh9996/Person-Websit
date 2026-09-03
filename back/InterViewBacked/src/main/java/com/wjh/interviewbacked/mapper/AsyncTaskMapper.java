package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.AsyncTask;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 异步任务 Mapper（MQ / 大模型 / 导出预留，本次仅建表对齐，预留 CRUD）。
 */
@Mapper
public interface AsyncTaskMapper {

    @Insert("INSERT INTO async_task (id, user_id, type, biz_id, status, payload, result, error_msg, retry_count, created_at, updated_at) " +
            "VALUES (#{id}, #{userId}, #{type}, #{bizId}, #{status}, #{payload}, #{result}, #{errorMsg}, #{retryCount}, #{createdAt}, #{updatedAt})")
    int insert(AsyncTask task);

    @Select("SELECT * FROM async_task WHERE id = #{id}")
    AsyncTask selectById(@Param("id") String id);

    @Select("SELECT * FROM async_task WHERE status = #{status} ORDER BY created_at ASC")
    List<AsyncTask> selectByStatus(@Param("status") String status);
}
