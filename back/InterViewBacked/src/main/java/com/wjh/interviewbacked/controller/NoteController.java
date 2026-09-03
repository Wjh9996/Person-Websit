package com.wjh.interviewbacked.controller;

import com.wjh.interviewbacked.common.ApiResult;
import com.wjh.interviewbacked.dto.NoteDTO;
import com.wjh.interviewbacked.dto.NoteDraft;
import com.wjh.interviewbacked.dto.TagCount;
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

    /** GET /api/notes?keyword=&category=&tag=&userId= */
    @GetMapping
    public ApiResult<List<NoteDTO>> list(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String category,
                                         @RequestParam(required = false) String tag,
                                         @RequestParam(required = false) String userId) {
        return ApiResult.ok(noteService.listNotes(userId, keyword, category, tag));
    }

    /** GET /api/notes/tags */
    @GetMapping("/tags")
    public ApiResult<List<TagCount>> tags() {
        return ApiResult.ok(noteService.listTags());
    }

    /** GET /api/notes/{id} */
    @GetMapping("/{id}")
    public ApiResult<NoteDTO> detail(@PathVariable String id) {
        return ApiResult.ok(noteService.getNote(id));
    }

    /** POST /api/notes（需登录，归属当前用户） */
    @PostMapping
    public ApiResult<NoteDTO> create(@Valid @RequestBody NoteDraft draft,
                                     @RequestAttribute("userId") String userId) {
        return ApiResult.ok(noteService.createNote(draft, userId));
    }

    /** PUT /api/notes/{id}（需登录，校验归属） */
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

    /** PATCH /api/notes/{id}/views（公开） */
    @PatchMapping("/{id}/views")
    public ApiResult<Void> increaseViews(@PathVariable String id) {
        noteService.increaseViews(id);
        return ApiResult.ok();
    }
}
