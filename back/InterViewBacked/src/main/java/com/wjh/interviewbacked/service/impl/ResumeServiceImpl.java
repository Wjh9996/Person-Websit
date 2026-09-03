package com.wjh.interviewbacked.service.impl;

import com.wjh.interviewbacked.common.JacksonUtils;
import com.wjh.interviewbacked.dto.ResumeData;
import com.wjh.interviewbacked.dto.ResumeNavItem;
import com.wjh.interviewbacked.dto.ResumeSnapshot;
import com.wjh.interviewbacked.entity.Resume;
import com.wjh.interviewbacked.entity.User;
import com.wjh.interviewbacked.exception.BusinessException;
import com.wjh.interviewbacked.mapper.ResumeMapper;
import com.wjh.interviewbacked.mapper.UserMapper;
import com.wjh.interviewbacked.service.ResumeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResumeServiceImpl implements ResumeService {

    private static final String DEFAULT_ICON = "📄";
    /** 公开模板取自站长账号（username=admin）的简历，抹掉私人联系方式 */
    private static final String OWNER_USERNAME = "admin";

    private final ResumeMapper resumeMapper;
    private final UserMapper userMapper;

    public ResumeServiceImpl(ResumeMapper resumeMapper, UserMapper userMapper) {
        this.resumeMapper = resumeMapper;
        this.userMapper = userMapper;
    }

    @Override
    public ResumeSnapshot getSnapshot(String userId) {
        // 匿名游客：返回脱敏的公开模板（站长的简历，不含私人联系方式）
        if (userId == null) {
            return buildPublicTemplate();
        }
        // 已登录：仅返回该用户自己的简历，做到数据隔离
        return buildSnapshot(resumeMapper.selectByUserId(userId));
    }

    @Override
    @Transactional
    public ResumeNavItem createResume(ResumeData data, String label, String userId) {
        String id = "custom-" + System.currentTimeMillis();
        String resolvedLabel = label;
        if (resolvedLabel == null || resolvedLabel.isBlank()) {
            resolvedLabel = data != null && data.getBasicInfo() != null
                    ? (data.getBasicInfo().getTitle() != null ? data.getBasicInfo().getTitle()
                    : (data.getBasicInfo().getName() != null ? data.getBasicInfo().getName() : "未命名简历"))
                    : "未命名简历";
        }
        Resume resume = new Resume();
        resume.setId(id);
        resume.setUserId(userId == null ? "" : userId);
        resume.setLabel(resolvedLabel);
        resume.setPath("/resume/" + id);
        resume.setIcon(DEFAULT_ICON);
        resume.setContent(JacksonUtils.toJson(data));
        resume.setVersion(0);
        resume.setDeleted(0);
        LocalDateTime now = LocalDateTime.now();
        resume.setCreatedAt(now);
        resume.setUpdatedAt(now);
        resumeMapper.insert(resume);
        return new ResumeNavItem(id, resolvedLabel, resume.getPath(), DEFAULT_ICON);
    }

    @Override
    @Transactional
    public void saveResume(String id, ResumeData data, String userId) {
        Resume resume = resumeMapper.selectById(id);
        if (resume == null) throw new BusinessException("简历不存在");
        // 水平越权防护：只能改自己的简历
        if (!userId.equals(resume.getUserId())) {
            throw new BusinessException(403, "无权操作该简历");
        }
        resume.setContent(JacksonUtils.toJson(data));
        if (data != null && data.getBasicInfo() != null) {
            String t = data.getBasicInfo().getTitle();
            String n = data.getBasicInfo().getName();
            if (t != null && !t.isBlank()) resume.setLabel(t);
            else if (n != null && !n.isBlank()) resume.setLabel(n);
        }
        resume.setUpdatedAt(LocalDateTime.now());
        int rows = resumeMapper.updateWithVersion(resume);
        if (rows == 0) throw new BusinessException(409, "简历已被他人修改，请刷新后重试");
    }

    @Override
    public boolean deleteResume(String id, String userId) {
        Resume resume = resumeMapper.selectById(id);
        if (resume == null) return false;
        // 水平越权防护：只能删自己的简历
        if (!userId.equals(resume.getUserId())) {
            throw new BusinessException(403, "无权操作该简历");
        }
        resumeMapper.deleteById(id);
        return true;
    }

    /** 构建快照（不含脱敏逻辑），供本人数据与公开模板复用 */
    private ResumeSnapshot buildSnapshot(List<Resume> list) {
        Map<String, ResumeData> resumes = new LinkedHashMap<>();
        List<ResumeNavItem> navItems = new ArrayList<>();
        for (Resume r : list) {
            resumes.put(r.getId(), JacksonUtils.fromJson(r.getContent(), ResumeData.class));
            navItems.add(new ResumeNavItem(r.getId(), r.getLabel(), r.getPath(), r.getIcon()));
        }
        ResumeSnapshot snapshot = new ResumeSnapshot();
        snapshot.setResumes(resumes);
        snapshot.setNavItems(navItems);
        return snapshot;
    }

    /** 公开模板：站长（admin）的全部简历，抹掉 phone / email 等私人联系方式 */
    private ResumeSnapshot buildPublicTemplate() {
        User owner = userMapper.selectByUsername(OWNER_USERNAME);
        if (owner == null) {
            return buildSnapshot(new ArrayList<>());
        }
        List<Resume> ownerResumes = resumeMapper.selectByUserId(owner.getId());
        Map<String, ResumeData> resumes = new LinkedHashMap<>();
        List<ResumeNavItem> navItems = new ArrayList<>();
        for (Resume r : ownerResumes) {
            ResumeData data = JacksonUtils.fromJson(r.getContent(), ResumeData.class);
            sanitize(data);
            resumes.put(r.getId(), data);
            navItems.add(new ResumeNavItem(r.getId(), r.getLabel(), r.getPath(), r.getIcon()));
        }
        ResumeSnapshot snapshot = new ResumeSnapshot();
        snapshot.setResumes(resumes);
        snapshot.setNavItems(navItems);
        return snapshot;
    }

    /** 脱敏：抹掉私人联系方式，保留可公开的作品集内容（姓名/学校/技能/项目等） */
    private void sanitize(ResumeData data) {
        if (data != null && data.getBasicInfo() != null) {
            data.getBasicInfo().setPhone("");
            data.getBasicInfo().setEmail("");
        }
    }
}
