package com.wjh.interviewbacked.service.impl;

import com.wjh.interviewbacked.common.HashUtils;
import com.wjh.interviewbacked.common.JacksonUtils;
import com.wjh.interviewbacked.dto.NoteDTO;
import com.wjh.interviewbacked.dto.NoteDraft;
import com.wjh.interviewbacked.dto.TagCount;
import com.wjh.interviewbacked.entity.Note;
import com.wjh.interviewbacked.entity.NoteContent;
import com.wjh.interviewbacked.entity.NoteTagRel;
import com.wjh.interviewbacked.entity.Tag;
import com.wjh.interviewbacked.exception.BusinessException;
import com.wjh.interviewbacked.mapper.NoteContentMapper;
import com.wjh.interviewbacked.mapper.NoteAnalysisMapper;
import com.wjh.interviewbacked.mapper.NoteMapper;
import com.wjh.interviewbacked.mapper.NoteTagRelMapper;
import com.wjh.interviewbacked.mapper.TagMapper;
import com.wjh.interviewbacked.service.NoteService;
import tools.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;
    private final NoteContentMapper noteContentMapper;
    private final TagMapper tagMapper;
    private final NoteTagRelMapper noteTagRelMapper;
    private final NoteAnalysisMapper noteAnalysisMapper;

    public NoteServiceImpl(NoteMapper noteMapper, NoteContentMapper noteContentMapper,
                           TagMapper tagMapper, NoteTagRelMapper noteTagRelMapper,
                           NoteAnalysisMapper noteAnalysisMapper) {
        this.noteMapper = noteMapper;
        this.noteContentMapper = noteContentMapper;
        this.tagMapper = tagMapper;
        this.noteTagRelMapper = noteTagRelMapper;
        this.noteAnalysisMapper = noteAnalysisMapper;
    }

    @Override
    public List<NoteDTO> listMyNotes(String userId, String keyword, String category, String tag) {
        // 未登录/无归属一律空集，绝不退回全表查询
        if (userId == null || userId.isBlank()) return List.of();
        return noteMapper.listNotes(userId, keyword, category, tag).stream()
                .map(n -> toDto(n, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<NoteDTO> listPlazaNotes(String keyword, String category, String tag) {
        return noteMapper.listPlazaNotes(keyword, category, tag).stream()
                .map(n -> toDto(n, null))
                .collect(Collectors.toList());
    }

    @Override
    public NoteDTO getNote(String id, String userId) {
        Note note = noteMapper.selectById(id);
        if (note == null || Objects.equals(note.getDeleted(), 1)) return null;
        assertVisible(note, userId);
        String content = noteContentMapper.selectContentByNoteId(id);
        return toDto(note, content);
    }

    /**
     * 可见性收口：广场笔记（visibility=1）放行；私有笔记必须作者本人，否则 403。
     * 这是修复「A 的笔记被 B 看到」的核心判据。
     */
    private void assertVisible(Note note, String userId) {
        if (Integer.valueOf(1).equals(note.getVisibility())) return;
        if (userId != null && userId.equals(note.getUserId())) return;
        throw new BusinessException(403, "该笔记为私密笔记，仅作者可见");
    }

    @Override
    @Transactional
    public NoteDTO createNote(NoteDraft draft, String userId) {
        LocalDateTime now = LocalDateTime.now();
        String content = draft.getContent() == null ? "" : draft.getContent();
        String hash = HashUtils.md5Hex(content);

        Note note = new Note();
        note.setId(UUID.randomUUID().toString());
        note.setUserId(userId);
        note.setTitle(draft.getTitle());
        note.setSummary(draft.getSummary());
        note.setCategory(draft.getCategory());
        note.setPinned(Boolean.TRUE.equals(draft.getPinned()));
        note.setViews(0);
        note.setVersion(0);
        note.setContentHash(hash);
        note.setVisibility(Boolean.TRUE.equals(draft.getToPlaza()) ? 1 : 0);
        note.setDeleted(0);
        note.setCreatedAt(now);
        note.setUpdatedAt(now);
        note.setTags(writeTags(draft.getTags()));
        noteMapper.insert(note);

        noteContentMapper.insert(new NoteContent(note.getId(), content));
        resolveTags(userId, note.getId(), draft.getTags(), now);
        return getNote(note.getId(), userId);
    }

    @Override
    @Transactional
    public NoteDTO updateNote(String id, NoteDraft draft, String userId) {
        Note note = noteMapper.selectById(id);
        if (note == null) throw new BusinessException("笔记不存在");
        if (userId == null || !userId.equals(note.getUserId())) throw new BusinessException(403, "无权修改他人笔记");

        String content = draft.getContent() == null ? "" : draft.getContent();
        String hash = HashUtils.md5Hex(content);

        // toPlaza 未传表示不改动可见性；显式传 false 才是从广场收回
        int visibility = draft.getToPlaza() == null
                ? (note.getVisibility() == null ? 0 : note.getVisibility())
                : (Boolean.TRUE.equals(draft.getToPlaza()) ? 1 : 0);

        List<String> oldTags = parseTags(note.getTags());
        List<String> newTags = draft.getTags() == null ? List.of() : draft.getTags();
        boolean unchanged = hash.equals(note.getContentHash())
                && Objects.equals(note.getTitle(), draft.getTitle())
                && Objects.equals(note.getSummary(), draft.getSummary())
                && Objects.equals(note.getCategory(), draft.getCategory())
                && Objects.equals(note.getVisibility(), visibility)
                && new HashSet<>(oldTags).equals(new HashSet<>(newTags));
        if (unchanged) return getNote(id, userId);

        note.setTitle(draft.getTitle());
        note.setSummary(draft.getSummary());
        note.setCategory(draft.getCategory());
        note.setTags(writeTags(newTags));
        note.setVisibility(visibility);
        boolean contentChanged = !hash.equals(note.getContentHash());
        note.setContentHash(hash);
        note.setUpdatedAt(LocalDateTime.now());
        // updateWithVersion 以 note.getVersion()（当前库版本）作 WHERE 条件，0 行即版本冲突
        int rows = noteMapper.updateWithVersion(note);
        if (rows == 0) throw new BusinessException(409, "笔记已被他人修改，请刷新后重试");

        noteContentMapper.update(new NoteContent(id, content));
        if (contentChanged) noteAnalysisMapper.invalidateByNote(id);
        resolveTags(userId, id, newTags, LocalDateTime.now());
        return getNote(id, userId);
    }

    @Override
    public boolean deleteNote(String id, String userId) {
        Note note = noteMapper.selectById(id);
        if (note == null) return false;
        if (userId == null || !userId.equals(note.getUserId())) throw new BusinessException(403, "无权删除他人笔记");
        return noteMapper.softDelete(id, userId, LocalDateTime.now()) > 0;
    }

    @Override
    public NoteDTO togglePinned(String id, String userId) {
        Note note = noteMapper.selectById(id);
        if (note == null) throw new BusinessException("笔记不存在");
        if (userId == null || !userId.equals(note.getUserId())) throw new BusinessException(403, "无权操作他人笔记");
        Boolean pinned = !Boolean.TRUE.equals(note.getPinned());
        noteMapper.togglePin(id, pinned, userId, LocalDateTime.now());
        return getNote(id, userId);
    }

    @Override
    public NoteDTO togglePlaza(String id, String userId, boolean toPlaza) {
        Note note = noteMapper.selectById(id);
        if (note == null) throw new BusinessException("笔记不存在");
        if (userId == null || !userId.equals(note.getUserId())) throw new BusinessException(403, "无权操作他人笔记");
        int visibility = toPlaza ? 1 : 0;
        noteMapper.updateVisibility(id, userId, visibility, LocalDateTime.now());
        return getNote(id, userId);
    }

    @Override
    public void increaseViews(String id) {
        noteMapper.incrementViews(id);
    }

    @Override
    public List<TagCount> listTags(String userId) {
        // userId 为空 = 游客视角，只统计广场公开笔记的标签
        return tagMapper.selectTagCounts(userId);
    }

    // ---------- 标签双写：note.tags 冗余展示 + note_tag_rel 管理真相源 ----------

    @Transactional
    protected void resolveTags(String userId, String noteId, List<String> tagNames, LocalDateTime now) {
        noteTagRelMapper.deleteByNoteId(noteId);
        if (tagNames == null || tagNames.isEmpty()) return;
        for (String raw : tagNames) {
            if (raw == null) continue;
            String name = raw.trim();
            if (name.isEmpty()) continue;
            Tag tag = tagMapper.selectByUserIdAndName(userId, name);
            if (tag == null) {
                tag = new Tag();
                tag.setId(UUID.randomUUID().toString());
                tag.setUserId(userId);
                tag.setName(name);
                tag.setCreatedAt(now);
                tagMapper.insert(tag);
            }
            noteTagRelMapper.insert(new NoteTagRel(noteId, tag.getId()));
        }
    }

    private NoteDTO toDto(Note note, String content) {
        NoteDTO dto = new NoteDTO();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setSummary(note.getSummary());
        dto.setContent(content);
        dto.setCategory(note.getCategory());
        dto.setTags(parseTags(note.getTags()));
        dto.setPinned(note.getPinned());
        dto.setViews(note.getViews());
        dto.setVisibility(note.getVisibility() == null ? 0 : note.getVisibility());
        dto.setAuthorId(note.getAuthorId() != null ? note.getAuthorId() : note.getUserId());
        dto.setAuthorName(note.getAuthorName());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        return dto;
    }

    private List<String> parseTags(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        return JacksonUtils.fromJson(json, new TypeReference<List<String>>() {});
    }

    private String writeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return "[]";
        return JacksonUtils.toJson(tags.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).toList());
    }
}
