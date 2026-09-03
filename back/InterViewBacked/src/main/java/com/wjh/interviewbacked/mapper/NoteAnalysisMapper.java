package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.NoteAnalysis;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 笔记分析 Mapper（大模型结果预留，本次仅建表对齐，预留 CRUD）。
 */
@Mapper
public interface NoteAnalysisMapper {

    @Insert("INSERT INTO note_analysis (id, note_id, user_id, analysis_type, result, model, status, created_at, updated_at) " +
            "VALUES (#{id}, #{noteId}, #{userId}, #{analysisType}, #{result}, #{model}, #{status}, #{createdAt}, #{updatedAt})")
    int insert(NoteAnalysis analysis);

    @Select("SELECT * FROM note_analysis WHERE note_id = #{noteId} AND analysis_type = #{analysisType} ORDER BY created_at DESC LIMIT 1")
    NoteAnalysis selectLatest(@Param("noteId") String noteId, @Param("analysisType") String analysisType);
}
