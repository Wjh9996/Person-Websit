package com.wjh.interviewbacked.service;

import com.wjh.interviewbacked.dto.NoteDTO;
import com.wjh.interviewbacked.dto.NoteDraft;
import com.wjh.interviewbacked.dto.TagCount;

import java.util.List;

public interface NoteService {

    /**
     * 列表查询：关键字（ngram 全文）/ 分类 / 标签过滤，置顶优先、按更新时间倒序。
     * userId 为空返回全站公开笔记（deleted=0）；非空仅返回该用户笔记（个人中心视图）。
     */
    List<NoteDTO> listNotes(String userId, String keyword, String category, String tag);

    /** 详情（含正文） */
    NoteDTO getNote(String id);

    /** 新建（归属当前用户） */
    NoteDTO createNote(NoteDraft draft, String userId);

    /** 更新（校验归属 + 乐观锁） */
    NoteDTO updateNote(String id, NoteDraft draft, String userId);

    /** 软删除（校验归属），返回是否成功 */
    boolean deleteNote(String id, String userId);

    /** 切换置顶（校验归属） */
    NoteDTO togglePinned(String id, String userId);

    /** 阅读数 +1（公开） */
    void increaseViews(String id);

    /** 全站标签云聚合（公开） */
    List<TagCount> listTags();
}
