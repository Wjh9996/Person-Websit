package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    int insert(ChatMessage message);

    /** 取会话最近 limit 条（SQL 倒序取，Service 层反转成正序展示） */
    List<ChatMessage> selectRecent(@Param("sessionId") String sessionId, @Param("limit") int limit);

    int deleteBySession(@Param("sessionId") String sessionId);
}
