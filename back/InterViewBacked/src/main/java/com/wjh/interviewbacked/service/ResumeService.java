package com.wjh.interviewbacked.service;

import com.wjh.interviewbacked.dto.ResumeData;
import com.wjh.interviewbacked.dto.ResumeNavItem;
import com.wjh.interviewbacked.dto.ResumeSnapshot;

public interface ResumeService {

    /**
     * 获取整份快照：resumes（id->内容） + navItems。
     * userId 为 null（匿名/游客）→ 返回脱敏的公开模板（站长的简历抹掉私人联系方式）；
     * 已登录 → 仅返回该用户自己的简历（数据隔离）。
     */
    ResumeSnapshot getSnapshot(String userId);

    /** 新建简历，返回导航项（归属当前用户） */
    ResumeNavItem createResume(ResumeData data, String label, String userId);

    /** 保存（覆盖）某份简历，校验归属（非本人 → 403） */
    void saveResume(String id, ResumeData data, String userId);

    /** 删除，返回是否成功，校验归属（非本人 → 403） */
    boolean deleteResume(String id, String userId);
}
