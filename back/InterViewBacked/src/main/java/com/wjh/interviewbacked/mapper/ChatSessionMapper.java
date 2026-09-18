package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChatSessionMapper {

    int insert(ChatSession session);

    /** 我的会话列表（最近更新的在前） */
    List<ChatSession> selectByUser(@Param("userId") String userId);

    ChatSession selectById(@Param("id") String id);

    /** 软删除：仅本人可删 */
    int softDelete(@Param("id") String id, @Param("userId") String userId);

    int updateTitle(@Param("id") String id,
                    @Param("title") String title,
                    @Param("updatedAt") LocalDateTime updatedAt);
}
