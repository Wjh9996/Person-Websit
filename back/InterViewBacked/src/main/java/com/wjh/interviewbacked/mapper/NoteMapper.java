package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoteMapper {

    /**
     * 列表查询：默认按更新时间倒序、置顶优先。
     * 仅查 note 表（不含正文），keyword 走 ngram 全文索引，category/tag 可选过滤。
     * userId 为空时返回全站公开笔记（deleted=0）；非空时仅返回该用户笔记。
     */
    List<Note> listNotes(@Param("userId") String userId,
                         @Param("keyword") String keyword,
                         @Param("category") String category,
                         @Param("tag") String tag);

    Note selectById(@Param("id") String id);

    int insert(Note note);

    /** 更新元数据（乐观锁）：WHERE id AND version，返回影响行数，0 表示版本冲突 */
    int updateWithVersion(Note note);

    /** 软删除（回收站）：仅本人可删 */
    int softDelete(@Param("id") String id,
                   @Param("userId") String userId,
                   @Param("deletedAt") java.time.LocalDateTime deletedAt);

    int incrementViews(@Param("id") String id);

    /** 切换置顶（不 bump 版本号） */
    int togglePin(@Param("noteId") String noteId,
                  @Param("pinned") Boolean pinned,
                  @Param("userId") String userId,
                  @Param("updatedAt") java.time.LocalDateTime updatedAt);
}
