package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.NoteTagRel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NoteTagRelMapper {

    int insert(NoteTagRel rel);

    int deleteByNoteId(@Param("noteId") String noteId);
}
