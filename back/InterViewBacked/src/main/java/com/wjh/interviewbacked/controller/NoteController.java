package com.wjh.interviewbacked.controller;

import com.wjh.interviewbacked.common.ApiResult;
import com.wjh.interviewbacked.dto.NoteDTO;
import com.wjh.interviewbacked.dto.NoteDraft;
import com.wjh.interviewbacked.dto.TagCount;
import com.wjh.interviewbacked.exception.BusinessException;
import com.wjh.interviewbacked.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * GET /api/notes?scope=mine|plaza&keyword=&category=&tag=
     * scope=mine  -> 我的笔记（需登录，返回本人私有 + 已发广场的全部笔记）
     * scope=plaza -> 讨论广场（公开，任何人可见，未登录也能访问）
     * 不带 scope 时默认 plaza，保证游客侧永远只有公开数据。
     */
    @GetMapping
    public ApiResult<List<NoteDTO>> list(@RequestParam(required = false, defaultValue = "plaza") String scope,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String category,
                                         @RequestParam(required = false) String tag,
                                         @RequestAttribute(value = "userId", required = false) String userId) {
        boolean mine = "mine".equalsIgnoreCase(scope);
        if (mine) {
            if (userId == null) throw new BusinessException(401, "请先登录后再查看我的笔记");
            return ApiResult.ok(noteService.listMyNotes(userId, keyword, category, tag));
        }
        return ApiResult.ok(noteService.listPlazaNotes(keyword, category, tag));
    }

    /**
     * GET /api/notes/tags?scope=mine|plaza
     * 游客只统计广场公开笔记的标签，不泄露私有标签。
     */
    @GetMapping("/tags")
    public ApiResult<List<TagCount>> tags(@RequestParam(required = false, defaultValue = "plaza") String scope,
                                          @RequestAttribute(value = "userId", required = false) String userId) {
        String scoped = "mine".equalsIgnoreCase(scope) ? userId : null;
        return ApiResult.ok(noteService.listTags(scoped));
    }

    /**
     * GET /api/notes/{id}
     * 广场笔记任何人可读；私有笔记仅作者可读，其余一律 403（Service 层 assertVisible 兜底）。
     */
    @GetMapping("/{id}")
    public ApiResult<NoteDTO> detail(@PathVariable String id,
                                     @RequestAttribute(value = "userId", required = false) String userId) {
        return ApiResult.ok(noteService.getNote(id, userId));
    }

    /** POST /api/notes（需登录，归属当前用户，toPlaza=true 同时发到广场） */
    @PostMapping
    public ApiResult<NoteDTO> create(@Valid @RequestBody NoteDraft draft,
                                     @RequestAttribute("userId") String userId) {
        return ApiResult.ok(noteService.createNote(draft, userId));
    }

    /** PUT /api/notes/{id}（需登录，校验归属，可调整广场可见性） */
    @PutMapping("/{id}")
    public ApiResult<NoteDTO> update(@PathVariable String id,
                                     @Valid @RequestBody NoteDraft draft,
                                     @RequestAttribute("userId") String userId) {
        return ApiResult.ok(noteService.updateNote(id, draft, userId));
    }

    /** DELETE /api/notes/{id}（需登录，校验归属，软删除） */
    @DeleteMapping("/{id}")
    public ApiResult<Boolean> delete(@PathVariable String id,
                                     @RequestAttribute("userId") String userId) {
        return ApiResult.ok(noteService.deleteNote(id, userId));
    }

    /** PATCH /api/notes/{id}/pin（需登录，校验归属） */
    @PatchMapping("/{id}/pin")
    public ApiResult<NoteDTO> togglePin(@PathVariable String id,
                                        @RequestAttribute("userId") String userId) {
        return ApiResult.ok(noteService.togglePinned(id, userId));
    }

    /** PATCH /api/notes/{id}/plaza?toPlaza=true|false（需登录，校验归属，发布/收回广场） */
    @PatchMapping("/{id}/plaza")
    public ApiResult<NoteDTO> togglePlaza(@PathVariable String id,
                                          @RequestParam(defaultValue = "true") boolean toPlaza,
                                          @RequestAttribute("userId") String userId) {
        return ApiResult.ok(noteService.togglePlaza(id, userId, toPlaza));
    }

    /** PATCH /api/notes/{id}/views（公开） */
    @PatchMapping("/{id}/views")
    public ApiResult<Void> increaseViews(@PathVariable String id) {
        noteService.increaseViews(id);
        return ApiResult.ok();
    }
}
