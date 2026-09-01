package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User selectByUsername(@Param("username") String username);

    User selectById(@Param("id") String id);

    int insert(User user);

    int update(User user);
}
