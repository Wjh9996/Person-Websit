package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.File;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文件/附件元信息 Mapper（对象存储预留，本次仅建表对齐，预留 CRUD）。
 */
@Mapper
public interface FileMapper {

    @Insert("INSERT INTO file (id, user_id, note_id, bucket, object_key, url, name, ext, size, mime, created_at) " +
            "VALUES (#{id}, #{userId}, #{noteId}, #{bucket}, #{objectKey}, #{url}, #{name}, #{ext}, #{size}, #{mime}, #{createdAt})")
    int insert(File file);

    @Select("SELECT * FROM file WHERE id = #{id}")
    File selectById(@Param("id") String id);

    @Select("SELECT * FROM file WHERE note_id = #{noteId} ORDER BY created_at DESC")
    List<File> selectByNoteId(@Param("noteId") String noteId);
}
