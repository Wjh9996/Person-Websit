package com.wjh.interviewbacked.service.impl;

import com.wjh.interviewbacked.common.JacksonUtils;
import com.wjh.interviewbacked.dto.ResumeData;
import com.wjh.interviewbacked.dto.ResumeNavItem;
import com.wjh.interviewbacked.dto.ResumeSnapshot;
import com.wjh.interviewbacked.entity.Resume;
import com.wjh.interviewbacked.exception.BusinessException;
import com.wjh.interviewbacked.mapper.ResumeMapper;
import com.wjh.interviewbacked.service.ResumeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResumeServiceImpl implements ResumeService {

    private static final String DEFAULT_ICON = "📄";

    private final ResumeMapper resumeMapper;

    public ResumeServiceImpl(ResumeMapper resumeMapper) {
        this.resumeMapper = resumeMapper;
    }

    @Override
    public ResumeSnapshot getSnapshot() {
        List<Resume> all = resumeMapper.selectAll();
        Map<String, ResumeData> resumes = new LinkedHashMap<>();
        List<ResumeNavItem> navItems = new ArrayList<>();
        for (Resume r : all) {
            resumes.put(r.getId(), JacksonUtils.fromJson(r.getContent(), ResumeData.class));
            navItems.add(new ResumeNavItem(r.getId(), r.getLabel(), r.getPath(), r.getIcon()));
        }
        ResumeSnapshot snapshot = new ResumeSnapshot();
        snapshot.setResumes(resumes);
        snapshot.setNavItems(navItems);
        return snapshot;
    }

    @Override
    public ResumeNavItem createResume(ResumeData data, String label) {
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
        resume.setLabel(resolvedLabel);
        resume.setPath("/resume/" + id);
        resume.setIcon(DEFAULT_ICON);
        resume.setContent(JacksonUtils.toJson(data));
        LocalDateTime now = LocalDateTime.now();
        resume.setCreatedAt(now);
        resume.setUpdatedAt(now);
        resumeMapper.insert(resume);
        return new ResumeNavItem(id, resolvedLabel, resume.getPath(), DEFAULT_ICON);
    }

    @Override
    public void saveResume(String id, ResumeData data) {
        Resume resume = resumeMapper.selectById(id);
        if (resume == null) throw new BusinessException("简历不存在");
        resume.setContent(JacksonUtils.toJson(data));
        if (data != null && data.getBasicInfo() != null) {
            String t = data.getBasicInfo().getTitle();
            String n = data.getBasicInfo().getName();
            if (t != null && !t.isBlank()) resume.setLabel(t);
            else if (n != null && !n.isBlank()) resume.setLabel(n);
        }
        resume.setUpdatedAt(LocalDateTime.now());
        resumeMapper.update(resume);
    }

    @Override
    public boolean deleteResume(String id) {
        Resume resume = resumeMapper.selectById(id);
        if (resume == null) return false;
        resumeMapper.deleteById(id);
        return true;
    }
}
