package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoteMapper {

    List<Note> selectAll();

    Note selectById(@Param("id") String id);

    int insert(Note note);

    int update(Note note);

    int deleteById(@Param("id") String id);

    int incrementViews(@Param("id") String id);
}
