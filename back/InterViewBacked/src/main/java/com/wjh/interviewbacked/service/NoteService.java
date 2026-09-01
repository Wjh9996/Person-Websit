package com.wjh.interviewbacked.service;

import com.wjh.interviewbacked.dto.NoteDTO;
import com.wjh.interviewbacked.dto.NoteDraft;
import com.wjh.interviewbacked.dto.TagCount;

import java.util.List;

public interface NoteService {

    /** 列表查询：关键字 / 分类 / 标签过滤，置顶优先、按更新时间倒序 */
    List<NoteDTO> listNotes(String keyword, String category, String tag);

    /** 详情 */
    NoteDTO getNote(String id);

    /** 新建 */
    NoteDTO createNote(NoteDraft draft);

    /** 更新 */
    NoteDTO updateNote(String id, NoteDraft draft);

    /** 删除，返回是否成功 */
    boolean deleteNote(String id);

    /** 切换置顶，返回更新后对象 */
    NoteDTO togglePinned(String id);

    /** 阅读数 +1 */
    void increaseViews(String id);

    /** 全站标签聚合 */
    List<TagCount> listTags();
}
