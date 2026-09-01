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

    /** GET /api/notes?keyword=&category=&tag= */
    @GetMapping
    public ApiResult<List<NoteDTO>> list(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String category,
                                         @RequestParam(required = false) String tag) {
        return ApiResult.ok(noteService.listNotes(keyword, category, tag));
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

    /** POST /api/notes */
    @PostMapping
    public ApiResult<NoteDTO> create(@Valid @RequestBody NoteDraft draft) {
        return ApiResult.ok(noteService.createNote(draft));
    }

    /** PUT /api/notes/{id} */
    @PutMapping("/{id}")
    public ApiResult<NoteDTO> update(@PathVariable String id, @Valid @RequestBody NoteDraft draft) {
        return ApiResult.ok(noteService.updateNote(id, draft));
    }

    /** DELETE /api/notes/{id} */
    @DeleteMapping("/{id}")
    public ApiResult<Boolean> delete(@PathVariable String id) {
        return ApiResult.ok(noteService.deleteNote(id));
    }

    /** PATCH /api/notes/{id}/pin */
    @PatchMapping("/{id}/pin")
    public ApiResult<NoteDTO> togglePin(@PathVariable String id) {
        return ApiResult.ok(noteService.togglePinned(id));
    }

    /** PATCH /api/notes/{id}/views */
    @PatchMapping("/{id}/views")
    public ApiResult<Void> increaseViews(@PathVariable String id) {
        noteService.increaseViews(id);
        return ApiResult.ok();
    }
}
