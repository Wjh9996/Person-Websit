package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoteMapper {

    /**
     * 我的笔记列表：默认按更新时间倒序、置顶优先。
     * 仅查 note 表（不含正文），keyword 走 ngram 全文索引，category/tag 可选过滤。
     * userId 必须非空且已鉴权；为空时 SQL 走 {@code AND 1 = 0} 返回空集，
     * 绝不能退回「查全表」——那正是过去把别人的笔记查出来的根因。
     */
    List<Note> listNotes(@Param("userId") String userId,
                         @Param("keyword") String keyword,
                         @Param("category") String category,
                         @Param("tag") String tag);

    /**
     * 讨论广场列表：只返回 visibility = 1 的公开笔记，并 LEFT JOIN user 回填作者昵称。
     * 无需登录即可访问，因此这里绝不能带 userId 过滤。
     */
    List<Note> listPlazaNotes(@Param("keyword") String keyword,
                              @Param("category") String category,
                              @Param("tag") String tag);

    /**
     * 知识库召回：在当前用户的笔记里按关键词做全文检索（标题/摘要权重更高）。
     * 依赖 ngram 全文索引，带相关度 score，条数由 limit 控制。
     */
    List<Note> searchMine(@Param("userId") String userId,
                          @Param("keyword") String keyword,
                          @Param("limit") int limit);

    /** 全文索引不可用（未配 ngram）时的兜底：LIKE 模糊匹配 */
    List<Note> searchMineLike(@Param("userId") String userId,
                              @Param("keyword") String keyword,
                              @Param("limit") int limit);

    /** 按 id 查单条（含 visibility 与作者昵称），不做可见性过滤，由 Service 层判定权限 */
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

    /** 切换讨论广场可见性：visibility 0 私有 / 1 公开，仅本人可改 */
    int updateVisibility(@Param("id") String id,
                         @Param("userId") String userId,
                         @Param("visibility") Integer visibility,
                         @Param("updatedAt") java.time.LocalDateTime updatedAt);
}
