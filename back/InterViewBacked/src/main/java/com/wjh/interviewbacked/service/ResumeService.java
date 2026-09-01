package com.wjh.interviewbacked.service;

import com.wjh.interviewbacked.dto.ResumeData;
import com.wjh.interviewbacked.dto.ResumeNavItem;
import com.wjh.interviewbacked.dto.ResumeSnapshot;

public interface ResumeService {

    /** 获取整份快照：resumes（id->内容） + navItems */
    ResumeSnapshot getSnapshot();

    /** 新建简历，返回导航项 */
    ResumeNavItem createResume(ResumeData data, String label);

    /** 保存（覆盖）某份简历 */
    void saveResume(String id, ResumeData data);

    /** 删除，返回是否成功 */
    boolean deleteResume(String id);
}
