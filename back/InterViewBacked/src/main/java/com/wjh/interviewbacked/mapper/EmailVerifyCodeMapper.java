package com.wjh.interviewbacked.mapper;

import com.wjh.interviewbacked.entity.EmailVerifyCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmailVerifyCodeMapper {

    /** 取该邮箱该场景最新一条验证码（用于重发限流与校验） */
    EmailVerifyCode selectLatest(@Param("email") String email, @Param("scene") String scene);

    int insert(EmailVerifyCode code);

    /** 校验失败次数 +1 */
    int incrementFailCount(@Param("id") String id);

    /** 校验通过后标记已使用（一次性） */
    int markUsed(@Param("id") String id);
}
