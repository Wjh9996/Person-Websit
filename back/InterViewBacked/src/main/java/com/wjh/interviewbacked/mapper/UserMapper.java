package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User selectByUsername(@Param("username") String username);

    /** 按邮箱查询（注册需保证邮箱唯一） */
    User selectByEmail(@Param("email") String email);

    User selectById(@Param("id") String id);

    int insert(User user);

    int update(User user);

    /** 修改/重置密码：只更新密码列，避免整行覆盖 */
    int updatePassword(@Param("id") String id,
                       @Param("password") String password,
                       @Param("updatedAt") java.time.LocalDateTime updatedAt);

    /** 登录成功时更新最近登录时间 */
    int updateLastLogin(@Param("id") String id,
                        @Param("lastLoginAt") java.time.LocalDateTime lastLoginAt,
                        @Param("updatedAt") java.time.LocalDateTime updatedAt);
}
