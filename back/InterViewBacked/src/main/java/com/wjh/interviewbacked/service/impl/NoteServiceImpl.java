package com.wjh.interviewbacked.service.impl;

import com.wjh.interviewbacked.common.JacksonUtils;
import com.wjh.interviewbacked.dto.NoteDTO;
import com.wjh.interviewbacked.dto.NoteDraft;
import com.wjh.interviewbacked.dto.TagCount;
import com.wjh.interviewbacked.entity.Note;
import com.wjh.interviewbacked.exception.BusinessException;
import com.wjh.interviewbacked.mapper.NoteMapper;
import com.wjh.interviewbacked.service.NoteService;
import tools.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;

    public NoteServiceImpl(NoteMapper noteMapper) {
        this.noteMapper = noteMapper;
    }

    @Override
    public List<NoteDTO> listNotes(String keyword, String category, String tag) {
        List<Note> all = noteMapper.selectAll();
        String kw = (keyword == null ? "" : keyword).trim().toLowerCase();
        String cat = (category == null ? "all" : category);
        String tg = (tag == null ? "" : tag).trim();

        return all.stream()
                .filter(n -> "all".equals(cat) || cat.isEmpty() || cat.equals(n.getCategory()))
                .filter(n -> tg.isEmpty() || parseTags(n.getTags()).contains(tg))
                .filter(n -> {
                    if (kw.isEmpty()) return true;
                    return (n.getTitle() != null && n.getTitle().toLowerCase().contains(kw))
                            || (n.getSummary() != null && n.getSummary().toLowerCase().contains(kw))
                            || (n.getContent() != null && n.getContent().toLowerCase().contains(kw));
                })
                .sorted((a, b) -> {
                    boolean ap = Boolean.TRUE.equals(a.getPinned());
                    boolean bp = Boolean.TRUE.equals(b.getPinned());
                    if (ap != bp) return ap ? -1 : 1;
                    return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public NoteDTO getNote(String id) {
        Note note = noteMapper.selectById(id);
        if (note == null) return null;
        return toDto(note);
    }

    @Override
    public NoteDTO createNote(NoteDraft draft) {
        Note note = new Note();
        note.setId(UUID.randomUUID().toString());
        note.setTitle(draft.getTitle());
        note.setSummary(draft.getSummary());
        note.setContent(draft.getContent());
        note.setCategory(draft.getCategory());
        note.setTags(writeTags(draft.getTags()));
        note.setPinned(Boolean.TRUE.equals(draft.getPinned()));
        note.setViews(0);
        LocalDateTime now = LocalDateTime.now();
        note.setCreatedAt(now);
        note.setUpdatedAt(now);
        noteMapper.insert(note);
        return toDto(note);
    }

    @Override
    public NoteDTO updateNote(String id, NoteDraft draft) {
        Note note = noteMapper.selectById(id);
        if (note == null) return null;
        note.setTitle(draft.getTitle());
        note.setSummary(draft.getSummary());
        note.setContent(draft.getContent());
        note.setCategory(draft.getCategory());
        note.setTags(writeTags(draft.getTags()));
        if (draft.getPinned() != null) note.setPinned(draft.getPinned());
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.update(note);
        return toDto(note);
    }

    @Override
    public boolean deleteNote(String id) {
        Note note = noteMapper.selectById(id);
        if (note == null) return false;
        noteMapper.deleteById(id);
        return true;
    }

    @Override
    public NoteDTO togglePinned(String id) {
        Note note = noteMapper.selectById(id);
        if (note == null) throw new BusinessException("笔记不存在");
        note.setPinned(!Boolean.TRUE.equals(note.getPinned()));
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.update(note);
        return toDto(note);
    }

    @Override
    public void increaseViews(String id) {
        noteMapper.incrementViews(id);
    }

    @Override
    public List<TagCount> listTags() {
        Map<String, Integer> counter = new HashMap<>();
        noteMapper.selectAll().forEach(n -> parseTags(n.getTags()).forEach(t ->
                counter.put(t, counter.getOrDefault(t, 0) + 1)));
        return counter.entrySet().stream()
                .map(e -> new TagCount(e.getKey(), e.getValue()))
                .sorted((a, b) -> b.getCount().compareTo(a.getCount()))
                .collect(Collectors.toList());
    }

    private NoteDTO toDto(Note note) {
        NoteDTO dto = new NoteDTO();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setSummary(note.getSummary());
        dto.setContent(note.getContent());
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
        return JacksonUtils.toJson(tags);
    }
}
