package com.wjh.interviewbacked.service.impl;

import com.wjh.interviewbacked.dto.NoteDTO;
import com.wjh.interviewbacked.dto.NoteDraft;
import com.wjh.interviewbacked.entity.Note;
import com.wjh.interviewbacked.exception.BusinessException;
import com.wjh.interviewbacked.mapper.NoteAnalysisMapper;
import com.wjh.interviewbacked.mapper.NoteContentMapper;
import com.wjh.interviewbacked.mapper.NoteMapper;
import com.wjh.interviewbacked.mapper.NoteTagRelMapper;
import com.wjh.interviewbacked.mapper.TagMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 多用户数据隔离的单元测试（本项目安全设计的核心）。
 *
 * 覆盖两类判据：
 * 1. 读：可见性收口 —— 广场笔记谁都能看，私有笔记只有作者能看；
 * 2. 写：归属校验 —— 改/删/置顶/发布都必须本人，未登录（userId 为空）一律拒绝而不是 NPE。
 */
@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    private static final String AUTHOR = "user-a";
    private static final String OTHER = "user-b";
    private static final String NOTE_ID = "note-1";

    @Mock
    private NoteMapper noteMapper;
    @Mock
    private NoteContentMapper noteContentMapper;
    @Mock
    private TagMapper tagMapper;
    @Mock
    private NoteTagRelMapper noteTagRelMapper;
    @Mock
    private NoteAnalysisMapper noteAnalysisMapper;

    private NoteServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NoteServiceImpl(noteMapper, noteContentMapper, tagMapper, noteTagRelMapper, noteAnalysisMapper);
    }

    private Note note(String ownerId, int visibility) {
        Note note = new Note();
        note.setId(NOTE_ID);
        note.setUserId(ownerId);
        note.setTitle("MySQL 索引笔记");
        note.setSummary("B+ 树与最左前缀");
        note.setVisibility(visibility);
        note.setDeleted(0);
        note.setPinned(false);
        note.setViews(0);
        note.setVersion(0);
        note.setTags("[\"MySQL\"]");
        return note;
    }

    // ==================== 读：可见性收口 ====================

    @Test
    @DisplayName("广场笔记（visibility=1）：游客可读")
    void getNote_publicNote_guestCanRead() {
        when(noteMapper.selectById(NOTE_ID)).thenReturn(note(AUTHOR, 1));

        NoteDTO dto = service.getNote(NOTE_ID, null);

        assertNotNull(dto);
        assertEquals(NOTE_ID, dto.getId());
    }

    @Test
    @DisplayName("广场笔记：非作者登录用户也可读")
    void getNote_publicNote_otherUserCanRead() {
        when(noteMapper.selectById(NOTE_ID)).thenReturn(note(AUTHOR, 1));

        assertNotNull(service.getNote(NOTE_ID, OTHER));
    }

    @Test
    @DisplayName("私有笔记：作者本人可读，并能取到正文")
    void getNote_privateNote_authorCanRead() {
        when(noteMapper.selectById(NOTE_ID)).thenReturn(note(AUTHOR, 0));
        when(noteContentMapper.selectContentByNoteId(NOTE_ID)).thenReturn("正文内容");

        NoteDTO dto = service.getNote(NOTE_ID, AUTHOR);

        assertEquals("正文内容", dto.getContent());
    }

    @Test
    @DisplayName("私有笔记：其他登录用户访问被拒（403）")
    void getNote_privateNote_otherUserForbidden() {
        when(noteMapper.selectById(NOTE_ID)).thenReturn(note(AUTHOR, 0));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.getNote(NOTE_ID, OTHER));
        assertEquals(403, ex.getCode());
        assertEquals("该笔记为私密笔记，仅作者可见", ex.getMessage());
        verify(noteContentMapper, never()).selectContentByNoteId(anyString());
    }

    @Test
    @DisplayName("私有笔记：游客访问被拒（403）")
    void getNote_privateNote_guestForbidden() {
        when(noteMapper.selectById(NOTE_ID)).thenReturn(note(AUTHOR, 0));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.getNote(NOTE_ID, null));
        assertEquals(403, ex.getCode());
    }

    @Test
    @DisplayName("已删除笔记：一律当作不存在")
    void getNote_deletedNote_returnsNull() {
        Note deleted = note(AUTHOR, 1);
        deleted.setDeleted(1);
        when(noteMapper.selectById(NOTE_ID)).thenReturn(deleted);

        assertNull(service.getNote(NOTE_ID, AUTHOR));
    }

    @Test
    @DisplayName("笔记不存在：返回 null 而非抛异常")
    void getNote_missingNote_returnsNull() {
        when(noteMapper.selectById(NOTE_ID)).thenReturn(null);

        assertNull(service.getNote(NOTE_ID, AUTHOR));
    }

    // ==================== 列表：归属隔离 ====================

    @Test
    @DisplayName("我的笔记：未登录返回空集（绝不退回全表查询）")
    void listMyNotes_withoutUser_returnsEmpty() {
        List<NoteDTO> result = service.listMyNotes(null, null, null, null);

        assertTrue(result.isEmpty());
        verify(noteMapper, never()).listNotes(anyString(), any(), any(), any());
    }

    @Test
    @DisplayName("我的笔记：只按当前用户查询")
    void listMyNotes_withUser_queriesByUserId() {
        when(noteMapper.listNotes(AUTHOR, null, null, null)).thenReturn(List.of(note(AUTHOR, 0)));

        List<NoteDTO> result = service.listMyNotes(AUTHOR, null, null, null);

        assertEquals(1, result.size());
        verify(noteMapper).listNotes(eq(AUTHOR), eq(null), eq(null), eq(null));
    }

    // ==================== 写：归属校验 ====================

    @Test
    @DisplayName("修改他人笔记：403 且不落库")
    void updateNote_byOtherUser_forbidden() {
        when(noteMapper.selectById(NOTE_ID)).thenReturn(note(AUTHOR, 0));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateNote(NOTE_ID, new NoteDraft(), OTHER));
        assertEquals(403, ex.getCode());
        assertEquals("无权修改他人笔记", ex.getMessage());
        verify(noteMapper, never()).updateWithVersion(any(Note.class));
    }

    @Test
    @DisplayName("未登录修改笔记：403（而不是空指针）")
    void updateNote_withoutUser_forbidden() {
        when(noteMapper.selectById(NOTE_ID)).thenReturn(note(AUTHOR, 0));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateNote(NOTE_ID, new NoteDraft(), null));
        assertEquals(403, ex.getCode());
    }

    @Test
    @DisplayName("删除他人笔记：403 且不执行软删除")
    void deleteNote_byOtherUser_forbidden() {
        when(noteMapper.selectById(NOTE_ID)).thenReturn(note(AUTHOR, 0));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteNote(NOTE_ID, OTHER));
        assertEquals(403, ex.getCode());
        assertEquals("无权删除他人笔记", ex.getMessage());
        verify(noteMapper, never()).softDelete(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("删除自己的笔记：走软删除")
    void deleteNote_byOwner_succeeds() {
        when(noteMapper.selectById(NOTE_ID)).thenReturn(note(AUTHOR, 0));
        when(noteMapper.softDelete(eq(NOTE_ID), eq(AUTHOR), any())).thenReturn(1);

        assertTrue(service.deleteNote(NOTE_ID, AUTHOR));
        verify(noteMapper).softDelete(eq(NOTE_ID), eq(AUTHOR), any());
    }

    @Test
    @DisplayName("置顶他人笔记：403")
    void togglePinned_byOtherUser_forbidden() {
        when(noteMapper.selectById(NOTE_ID)).thenReturn(note(AUTHOR, 0));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.togglePinned(NOTE_ID, OTHER));
        assertEquals(403, ex.getCode());
        verify(noteMapper, never()).togglePin(anyString(), any(), anyString(), any());
    }

    @Test
    @DisplayName("把他人笔记发布到广场：403")
    void togglePlaza_byOtherUser_forbidden() {
        when(noteMapper.selectById(NOTE_ID)).thenReturn(note(AUTHOR, 0));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.togglePlaza(NOTE_ID, OTHER, true));
        assertEquals(403, ex.getCode());
        verify(noteMapper, never()).updateVisibility(anyString(), anyString(), eq(1), any());
    }
}
