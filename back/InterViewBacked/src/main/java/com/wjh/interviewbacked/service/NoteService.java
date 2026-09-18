package com.wjh.interviewbacked.service;

import com.wjh.interviewbacked.dto.NoteDTO;
import com.wjh.interviewbacked.dto.NoteDraft;
import com.wjh.interviewbacked.dto.TagCount;

import java.util.List;

public interface NoteService {

    /**
     * 我的笔记列表：必须带已鉴权的 userId，为空直接返回空列表（不查库）。
     * 支持关键字（ngram 全文）/ 分类 / 标签过滤，置顶优先、按更新时间倒序。
     * 返回本人全部笔记（含私有的和已发到广场的）。
     */
    List<NoteDTO> listMyNotes(String userId, String keyword, String category, String tag);

    /**
     * 讨论广场列表：仅 visibility = 1 的公开笔记，无需登录，所有人可见。
     * 列表不含正文，但会带上作者昵称便于广场展示。
     */
    List<NoteDTO> listPlazaNotes(String keyword, String category, String tag);

    /**
     * 详情（含正文）。
     * 广场笔记对所有人放行；私有笔记要求 userId 与作者一致，否则抛 403，杜绝水平越权偷看。
     */
    NoteDTO getNote(String id, String userId);

    /** 新建（归属当前用户，toPlaza=true 时同步发布到广场） */
    NoteDTO createNote(NoteDraft draft, String userId);

    /** 更新（校验归属 + 乐观锁，可同时调整广场可见性） */
    NoteDTO updateNote(String id, NoteDraft draft, String userId);

    /** 软删除（校验归属），返回是否成功 */
    boolean deleteNote(String id, String userId);

    /** 切换置顶（校验归属） */
    NoteDTO togglePinned(String id, String userId);

    /** 切换广场可见性（校验归属）：toPlaza=true 公开，false 收回为私有 */
    NoteDTO togglePlaza(String id, String userId, boolean toPlaza);

    /** 阅读数 +1（广场公开调用） */
    void increaseViews(String id);

    /**
     * 标签云聚合。userId 非空则统计该用户自己的笔记标签；
     * 为空（游客）只统计讨论广场的公开笔记标签，避免泄露他人私有标签。
     */
    List<TagCount> listTags(String userId);
}
