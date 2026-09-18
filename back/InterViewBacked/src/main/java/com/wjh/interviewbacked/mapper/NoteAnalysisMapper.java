package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.NoteAnalysis;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 笔记分析 Mapper（大模型结果，已接业务逻辑）。
 */
@Mapper
public interface NoteAnalysisMapper {

    @Insert("INSERT INTO note_analysis (id, note_id, user_id, analysis_type, result, model, status, created_at, updated_at) " +
            "VALUES (#{id}, #{noteId}, #{userId}, #{analysisType}, #{result}, #{model}, #{status}, #{createdAt}, #{updatedAt})")
    int insert(NoteAnalysis analysis);

    @Select("SELECT * FROM note_analysis WHERE note_id = #{noteId} AND analysis_type = #{analysisType} ORDER BY created_at DESC LIMIT 1")
    NoteAnalysis selectLatest(@Param("noteId") String noteId, @Param("analysisType") String analysisType);

    /** 该笔记全部有效分析结果（status=1），按更新时间倒序 */
    @Select("SELECT * FROM note_analysis WHERE note_id = #{noteId} AND status = 1 ORDER BY updated_at DESC")
    List<NoteAnalysis> selectByNote(@Param("noteId") String noteId);

    /** 正文变更后置旧分析失效（status=0），下次分析重新生成 */
    @Update("UPDATE note_analysis SET status = 0 WHERE note_id = #{noteId} AND status = 1")
    int invalidateByNote(@Param("noteId") String noteId);
}
