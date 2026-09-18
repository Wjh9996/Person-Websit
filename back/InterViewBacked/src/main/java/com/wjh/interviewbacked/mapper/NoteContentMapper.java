package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.NoteContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoteContentMapper {

    /** 按笔记 id 取正文（详情接口专用） */
    String selectContentByNoteId(@Param("noteId") String noteId);

    /** 批量取正文（知识库问答一次取多篇，避免 N 次单查） */
    List<NoteContent> selectByNoteIds(@Param("ids") List<String> ids);

    int insert(NoteContent content);

    /** 覆盖更新正文 */
    int update(NoteContent content);

    int deleteByNoteId(@Param("noteId") String noteId);
}
