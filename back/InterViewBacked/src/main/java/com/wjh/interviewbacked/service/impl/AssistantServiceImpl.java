package com.wjh.interviewbacked.service.impl;

import com.wjh.interviewbacked.common.JacksonUtils;
import com.wjh.interviewbacked.component.LlmClient;
import com.wjh.interviewbacked.dto.AskResult;
import com.wjh.interviewbacked.dto.ChatMessageVO;
import com.wjh.interviewbacked.dto.ChatRef;
import com.wjh.interviewbacked.dto.ChatSessionVO;
import com.wjh.interviewbacked.entity.ChatMessage;
import com.wjh.interviewbacked.entity.ChatSession;
import com.wjh.interviewbacked.entity.Note;
import com.wjh.interviewbacked.entity.NoteContent;
import com.wjh.interviewbacked.exception.BusinessException;
import com.wjh.interviewbacked.mapper.ChatMessageMapper;
import com.wjh.interviewbacked.mapper.ChatSessionMapper;
import com.wjh.interviewbacked.mapper.NoteContentMapper;
import com.wjh.interviewbacked.mapper.NoteMapper;
import com.wjh.interviewbacked.service.AssistantService;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * AI 助手实现（RAG）：
 * 1. 召回：用 MySQL 全文检索在「当前用户自己的笔记」里找 Top-N 相关笔记；
 * 2. 生成：把笔记正文 + 最近几轮对话拼进 Prompt，让大模型只依据笔记作答；
 * 3. 溯源：答案落库时保存引用的笔记，前端可点回原文。
 *
 * 注意：召回与取正文都强制带 user_id，确保 A 用户永远检索不到 B 的笔记。
 */
@Service
public class AssistantServiceImpl implements AssistantService {

    /** 召回笔记数 */
    private static final int TOP_N = 5;
    /** 单篇笔记送入模型的最大字符数（控制 token） */
    private static final int PER_NOTE_LIMIT = 2500;
    /** 携带的历史消息条数（多轮上下文） */
    private static final int HISTORY_LIMIT = 6;

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final NoteMapper noteMapper;
    private final NoteContentMapper noteContentMapper;
    private final LlmClient llmClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AssistantServiceImpl(ChatSessionMapper sessionMapper, ChatMessageMapper messageMapper,
                                NoteMapper noteMapper, NoteContentMapper noteContentMapper,
                                LlmClient llmClient) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.noteMapper = noteMapper;
        this.noteContentMapper = noteContentMapper;
        this.llmClient = llmClient;
    }

    @Override
    public List<ChatSessionVO> listSessions(String userId) {
        return sessionMapper.selectByUser(userId).stream()
                .map(s -> new ChatSessionVO(s.getId(), s.getTitle(), s.getUpdatedAt()))
                .toList();
    }

    @Override
    public ChatSessionVO createSession(String userId, String title) {
        LocalDateTime now = LocalDateTime.now();
        ChatSession session = new ChatSession();
        session.setId(UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setTitle(title == null || title.isBlank() ? "新对话" : title.trim());
        session.setDeleted(0);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        sessionMapper.insert(session);
        return new ChatSessionVO(session.getId(), session.getTitle(), session.getUpdatedAt());
    }

    @Override
    public boolean deleteSession(String sessionId, String userId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) return false;
        if (!userId.equals(session.getUserId())) throw new BusinessException(403, "无权删除他人会话");
        return sessionMapper.softDelete(sessionId, userId) > 0;
    }

    @Override
    public List<ChatMessageVO> listMessages(String sessionId, String userId) {
        assertOwner(sessionId, userId);
        List<ChatMessage> list = messageMapper.selectRecent(sessionId, 200);
        Collections.reverse(list);
        return list.stream().map(this::toVo).toList();
    }

    @Override
    public AskResult ask(String sessionId, String userId, String question) {
        ChatSession session = assertOwner(sessionId, userId);
        LocalDateTime now = LocalDateTime.now();

        // ① 先落用户问题（即使后面失败，问答历史也完整）
        saveMessage(sessionId, userId, "user", question, null, now);

        // ② 召回：只在自己的笔记里找。
        //   追问（"再详细说说第二点"）这种短句检索不到东西，回退带上上一轮的问题一起检索。
        String lastQuestion = findLastQuestion(sessionId, question);
        String recallKeyword = question.length() < 12 && lastQuestion != null
                ? lastQuestion + " " + question
                : question;
        List<Note> hits = recall(userId, recallKeyword);
        List<ChatRef> refs = new ArrayList<>();
        StringBuilder noteBlock = new StringBuilder();
        if (!hits.isEmpty()) {
            List<String> ids = hits.stream().map(Note::getId).toList();
            Map<String, String> contents = noteContentMapper.selectByNoteIds(ids).stream()
                    .collect(java.util.stream.Collectors.toMap(NoteContent::getNoteId, c -> c.getContent() == null ? "" : c.getContent(), (a, b) -> a));
            for (int i = 0; i < hits.size(); i++) {
                Note n = hits.get(i);
                refs.add(new ChatRef(n.getId(), n.getTitle(), n.getScore()));
                String body = contents.getOrDefault(n.getId(), "");
                if (body.length() > PER_NOTE_LIMIT) body = body.substring(0, PER_NOTE_LIMIT) + "…（已截断）";
                noteBlock.append("[").append(i + 1).append("] id=").append(n.getId())
                        .append(" 标题=").append(n.getTitle()).append("\n")
                        .append(body).append("\n\n");
            }
        }

        // ③ 历史上下文（不含本次问题）
        List<ChatMessage> history = messageMapper.selectRecent(sessionId, HISTORY_LIMIT + 2);
        Collections.reverse(history);
        StringBuilder historyBlock = new StringBuilder();
        for (ChatMessage m : history) {
            if (m.getContent() == null) continue;
            if (m.getRole().equals("user") && m.getContent().equals(question)) continue; // 本次问题已单独拼
            historyBlock.append(m.getRole().equals("user") ? "用户：" : "助手：")
                    .append(m.getContent().length() > 500 ? m.getContent().substring(0, 500) + "…" : m.getContent())
                    .append("\n");
        }

        String answer;
        boolean fromKb = !hits.isEmpty();
        if (!fromKb && historyBlock.length() == 0) {
            // 知识库里一篇都没命中，且没有上下文：不浪费 token，直接引导用户
            answer = "我在你的笔记里没有找到与「" + question + "」相关的内容。\n" +
                    "可以换个更具体的关键词（比如笔记里出现过的术语），或先写一篇相关笔记再来提问。";
        } else {
            String userPrompt = "历史对话：\n" + (historyBlock.length() == 0 ? "（无）\n" : historyBlock)
                    + "\n参考笔记：\n" + (noteBlock.length() == 0 ? "（本次未检索到相关笔记）\n" : noteBlock)
                    + "\n问题：" + question;
            String json = llmClient.analyze(systemPrompt(), userPrompt, 1500);
            answer = parseAnswer(json, refs);
        }

        // ④ 落助手答案 + 引用
        ChatMessage saved = saveMessage(sessionId, userId, "assistant", answer, refs, LocalDateTime.now());

        // ⑤ 首条提问时把会话标题改成问题摘要
        if ("新对话".equals(session.getTitle())) {
            String title = question.length() > 20 ? question.substring(0, 20) + "…" : question;
            sessionMapper.updateTitle(sessionId, title, LocalDateTime.now());
        }
        return new AskResult(saved.getId(), answer, refs, fromKb);
    }

    /* ===================== 内部方法 ===================== */

    private ChatSession assertOwner(String sessionId, String userId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || session.getDeleted() != null && session.getDeleted() == 1) {
            throw new BusinessException("会话不存在");
        }
        if (!userId.equals(session.getUserId())) throw new BusinessException(403, "无权访问他人会话");
        return session;
    }

    /** 取上一轮用户问题（用于追问场景的召回补词），没有则 null */
    private String findLastQuestion(String sessionId, String currentQuestion) {
        List<ChatMessage> recent = messageMapper.selectRecent(sessionId, 10);
        for (ChatMessage m : recent) {
            if ("user".equals(m.getRole()) && m.getContent() != null && !m.getContent().equals(currentQuestion)) {
                return m.getContent();
            }
        }
        return null;
    }

    /** 全文检索优先，索引不可用时退化为 LIKE，保证功能永远可用 */
    private List<Note> recall(String userId, String keyword) {
        List<Note> hits = new ArrayList<>();
        try {
            hits = noteMapper.searchMine(userId, keyword, TOP_N);
        } catch (Exception ignored) {
            // 未配 ngram 或索引缺失时走兜底
        }
        if (hits.isEmpty()) {
            hits = noteMapper.searchMineLike(userId, keyword, TOP_N);
        }
        return hits;
    }

    private ChatMessage saveMessage(String sessionId, String userId, String role,
                                    String content, List<ChatRef> refs, LocalDateTime now) {
        ChatMessage m = new ChatMessage();
        m.setId(UUID.randomUUID().toString());
        m.setSessionId(sessionId);
        m.setUserId(userId);
        m.setRole(role);
        m.setContent(content);
        m.setRefs(refs == null || refs.isEmpty() ? null : JacksonUtils.toJson(refs));
        m.setCreatedAt(now);
        messageMapper.insert(m);
        return m;
    }

    private ChatMessageVO toVo(ChatMessage m) {
        List<ChatRef> refs = null;
        if (m.getRefs() != null && !m.getRefs().isBlank()) {
            try {
                refs = JacksonUtils.fromJson(m.getRefs(), new TypeReference<List<ChatRef>>() {});
            } catch (Exception ignored) {
                refs = null;
            }
        }
        return new ChatMessageVO(m.getId(), m.getRole(), m.getContent(), refs, m.getCreatedAt());
    }

    /** 解析模型输出，失败时降级为原文展示（保证用户总能看到内容） */
    @SuppressWarnings("unchecked")
    private String parseAnswer(String json, List<ChatRef> refs) {
        if (json == null || json.isBlank()) return "模型没有返回内容，请稍后重试。";
        try {
            Map<String, Object> node = objectMapper.readValue(json, Map.class);
            Object answer = node.get("answer");
            if (answer == null) answer = node.get("content");
            if (answer == null) return json;
            // 模型若指出了用到的笔记 id，就把引用收敛到这些 id，避免"引用了但没用到"
            Object used = node.get("usedNoteIds");
            if (used instanceof List<?> list && !list.isEmpty()) {
                Set<String> usedIds = new HashSet<>(list.stream().map(String::valueOf).toList());
                refs.removeIf(ref -> !usedIds.contains(ref.getNoteId()));
            }
            return String.valueOf(answer);
        } catch (Exception e) {
            return json;
        }
    }

    private String systemPrompt() {
        return "你是用户的私人知识库助手，优先依据【参考笔记】回答问题。\n" +
                "规则：\n" +
                "1. 回答用中文，条理清晰，可用要点/小标题；不要复述问题。\n" +
                "2. 参考笔记是检索出来的，可能不相关：\n" +
                "   - 相关 → 依据它回答，并在末尾标注引用编号；\n" +
                "   - 不相关但【历史对话】里有答案（例如用户在追问上一轮）→ 基于历史对话继续回答，并说明「依据上一轮对话」；\n" +
                "   - 都没有 → 明确说明「笔记里没有提到」，严禁编造。\n" +
                "3. usedNoteIds 只填真正用到的笔记 id；一篇没用到就填空数组。\n" +
                "4. 仅输出 JSON：{\"answer\":\"...\",\"usedNoteIds\":[\"id1\"]}，不要输出其他任何内容。";
    }
}
