package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.NoteShare;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 笔记分享只读链接 Mapper（阶段二功能，本次仅建表对齐，预留 CRUD）。
 */
@Mapper
public interface NoteShareMapper {

    @Insert("INSERT INTO note_share (id, note_id, token, created_by, expires_at, created_at) " +
            "VALUES (#{id}, #{noteId}, #{token}, #{createdBy}, #{expiresAt}, #{createdAt})")
    int insert(NoteShare share);

    @Select("SELECT * FROM note_share WHERE token = #{token}")
    NoteShare selectByToken(@Param("token") String token);

    @Select("SELECT * FROM note_share WHERE note_id = #{noteId}")
    List<NoteShare> selectByNoteId(@Param("noteId") String noteId);
}
