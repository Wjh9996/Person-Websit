package com.wjh.interviewbacked.controller;

import com.wjh.interviewbacked.common.ApiResult;
import com.wjh.interviewbacked.dto.AskRequest;
import com.wjh.interviewbacked.dto.AskResult;
import com.wjh.interviewbacked.dto.ChatMessageVO;
import com.wjh.interviewbacked.dto.ChatSessionVO;
import com.wjh.interviewbacked.exception.BusinessException;
import com.wjh.interviewbacked.service.AssistantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 助手（基于自己笔记的知识库问答）
 *
 * 除 POST/PATCH/DELETE 由拦截器强制鉴权外，GET 也需要登录，
 * 因此这里一律用 required = false 取 userId 后手动判空，避免游客读到他人会话。
 */
@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    /** GET /api/assistant/sessions */
    @GetMapping("/sessions")
    public ApiResult<List<ChatSessionVO>> sessions(@RequestAttribute(value = "userId", required = false) String userId) {
        return ApiResult.ok(assistantService.listSessions(requireUser(userId)));
    }

    /** POST /api/assistant/sessions，body 可传 { "title": "..." } */
    @PostMapping("/sessions")
    public ApiResult<ChatSessionVO> create(@RequestBody(required = false) Map<String, String> body,
                                           @RequestAttribute("userId") String userId) {
        String title = body == null ? null : body.get("title");
        return ApiResult.ok(assistantService.createSession(userId, title));
    }

    /** DELETE /api/assistant/sessions/{id} */
    @DeleteMapping("/sessions/{id}")
    public ApiResult<Boolean> remove(@PathVariable String id,
                                     @RequestAttribute("userId") String userId) {
        return ApiResult.ok(assistantService.deleteSession(id, userId));
    }

    /** GET /api/assistant/sessions/{id}/messages */
    @GetMapping("/sessions/{id}/messages")
    public ApiResult<List<ChatMessageVO>> messages(@PathVariable String id,
                                                   @RequestAttribute(value = "userId", required = false) String userId) {
        return ApiResult.ok(assistantService.listMessages(id, requireUser(userId)));
    }

    /** POST /api/assistant/sessions/{id}/ask，body: { "question": "..." } */
    @PostMapping("/sessions/{id}/ask")
    public ApiResult<AskResult> ask(@PathVariable String id,
                                    @Valid @RequestBody AskRequest request,
                                    @RequestAttribute("userId") String userId) {
        return ApiResult.ok(assistantService.ask(id, userId, request.getQuestion()));
    }

    private String requireUser(String userId) {
        if (userId == null || userId.isBlank()) throw new BusinessException(401, "请先登录后再使用 AI 助手");
        return userId;
    }
}
