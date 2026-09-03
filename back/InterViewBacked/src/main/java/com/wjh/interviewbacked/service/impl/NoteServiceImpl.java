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

    public NoteServiceImpl(NoteMapper noteMapper, NoteContentMapper noteContentMapper,
                           TagMapper tagMapper, NoteTagRelMapper noteTagRelMapper) {
        this.noteMapper = noteMapper;
        this.noteContentMapper = noteContentMapper;
        this.tagMapper = tagMapper;
        this.noteTagRelMapper = noteTagRelMapper;
    }

    @Override
    public List<NoteDTO> listNotes(String userId, String keyword, String category, String tag) {
        return noteMapper.listNotes(userId, keyword, category, tag).stream()
                .map(n -> toDto(n, null))
                .collect(Collectors.toList());
    }

    @Override
    public NoteDTO getNote(String id) {
        Note note = noteMapper.selectById(id);
        if (note == null || Boolean.TRUE.equals(Objects.equals(note.getDeleted(), 1))) return null;
        String content = noteContentMapper.selectContentByNoteId(id);
        return toDto(note, content);
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
        note.setDeleted(0);
        note.setCreatedAt(now);
        note.setUpdatedAt(now);
        note.setTags(writeTags(draft.getTags()));
        noteMapper.insert(note);

        noteContentMapper.insert(new NoteContent(note.getId(), content));
        resolveTags(userId, note.getId(), draft.getTags(), now);
        return getNote(note.getId());
    }

    @Override
    @Transactional
    public NoteDTO updateNote(String id, NoteDraft draft, String userId) {
        Note note = noteMapper.selectById(id);
        if (note == null) throw new BusinessException("笔记不存在");
        if (!userId.equals(note.getUserId())) throw new BusinessException(403, "无权修改他人笔记");

        String content = draft.getContent() == null ? "" : draft.getContent();
        String hash = HashUtils.md5Hex(content);

        // 后端去重：内容与其他字段均无变化则直接返回，不 bump 版本、不写库
        List<String> oldTags = parseTags(note.getTags());
        List<String> newTags = draft.getTags() == null ? List.of() : draft.getTags();
        boolean unchanged = hash.equals(note.getContentHash())
                && Objects.equals(note.getTitle(), draft.getTitle())
                && Objects.equals(note.getSummary(), draft.getSummary())
                && Objects.equals(note.getCategory(), draft.getCategory())
                && new HashSet<>(oldTags).equals(new HashSet<>(newTags));
        if (unchanged) return getNote(id);

        note.setTitle(draft.getTitle());
        note.setSummary(draft.getSummary());
        note.setCategory(draft.getCategory());
        note.setTags(writeTags(newTags));
        note.setContentHash(hash);
        note.setUpdatedAt(LocalDateTime.now());
        // updateWithVersion 以 note.getVersion()（当前库版本）作 WHERE 条件，0 行即版本冲突
        int rows = noteMapper.updateWithVersion(note);
        if (rows == 0) throw new BusinessException(409, "笔记已被他人修改，请刷新后重试");

        noteContentMapper.update(new NoteContent(id, content));
        resolveTags(userId, id, newTags, LocalDateTime.now());
        return getNote(id);
    }

    @Override
    public boolean deleteNote(String id, String userId) {
        Note note = noteMapper.selectById(id);
        if (note == null) return false;
        if (!userId.equals(note.getUserId())) throw new BusinessException(403, "无权删除他人笔记");
        return noteMapper.softDelete(id, userId, LocalDateTime.now()) > 0;
    }

    @Override
    public NoteDTO togglePinned(String id, String userId) {
        Note note = noteMapper.selectById(id);
        if (note == null) throw new BusinessException("笔记不存在");
        if (!userId.equals(note.getUserId())) throw new BusinessException(403, "无权操作他人笔记");
        Boolean pinned = !Boolean.TRUE.equals(note.getPinned());
        noteMapper.togglePin(id, pinned, userId, LocalDateTime.now());
        return getNote(id);
    }

    @Override
    public void increaseViews(String id) {
        noteMapper.incrementViews(id);
    }

    @Override
    public List<TagCount> listTags() {
        return tagMapper.selectTagCounts();
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
