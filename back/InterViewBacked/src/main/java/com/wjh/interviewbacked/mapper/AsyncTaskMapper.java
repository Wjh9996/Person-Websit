package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.AsyncTask;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 异步任务 Mapper（MQ / 大模型 / 导出；当前用数据库轮询，未来可平滑换 MQ 消费者）。
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

    @Update("UPDATE async_task SET status = #{status}, result = #{result}, error_msg = #{errorMsg}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("status") String status,
                     @Param("result") String result, @Param("errorMsg") String errorMsg,
                     @Param("updatedAt") LocalDateTime updatedAt);
}
