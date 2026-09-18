package com.wjh.interviewbacked.service;

import com.wjh.interviewbacked.dto.AskResult;
import com.wjh.interviewbacked.dto.ChatMessageVO;
import com.wjh.interviewbacked.dto.ChatSessionVO;

import java.util.List;

/**
 * AI 助手：以「自己的笔记」为知识库做智能问答（RAG）。
 * 流程：全文检索召回相关笔记 → 拼进 Prompt → 大模型作答 → 落库并带引用来源。
 */
public interface AssistantService {

    /** 我的会话列表 */
    List<ChatSessionVO> listSessions(String userId);

    /** 新建会话（title 可空，默认「新对话」） */
    ChatSessionVO createSession(String userId, String title);

    /** 删除会话（软删除，校验归属） */
    boolean deleteSession(String sessionId, String userId);

    /** 会话消息（按时间正序） */
    List<ChatMessageVO> listMessages(String sessionId, String userId);

    /** 提问：同步返回答案与引用 */
    AskResult ask(String sessionId, String userId, String question);
}
