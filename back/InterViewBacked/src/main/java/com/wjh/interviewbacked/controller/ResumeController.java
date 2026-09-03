package com.wjh.interviewbacked.controller;

import com.wjh.interviewbacked.common.ApiResult;
import com.wjh.interviewbacked.dto.CreateResumeRequest;
import com.wjh.interviewbacked.dto.ResumeData;
import com.wjh.interviewbacked.dto.ResumeNavItem;
import com.wjh.interviewbacked.dto.ResumeSnapshot;
import com.wjh.interviewbacked.service.ResumeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    /** GET /api/resumes —— 返回整份快照（匿名=脱敏公开模板，登录=本人简历） */
    @GetMapping
    public ApiResult<ResumeSnapshot> snapshot(
            @RequestAttribute(value = "userId", required = false) String userId) {
        return ApiResult.ok(resumeService.getSnapshot(userId));
    }

    /** POST /api/resumes（需登录，归属当前用户） */
    @PostMapping
    public ApiResult<ResumeNavItem> create(@Valid @RequestBody CreateResumeRequest req,
                                            @RequestAttribute("userId") String userId) {
        return ApiResult.ok(resumeService.createResume(req.getData(), req.getLabel(), userId));
    }

    /** PUT /api/resumes/{id}（需登录，校验归属） */
    @PutMapping("/{id}")
    public ApiResult<Void> save(@PathVariable String id,
                                @RequestBody ResumeData data,
                                @RequestAttribute("userId") String userId) {
        resumeService.saveResume(id, data, userId);
        return ApiResult.ok();
    }

    /** DELETE /api/resumes/{id}（需登录，校验归属） */
    @DeleteMapping("/{id}")
    public ApiResult<Boolean> delete(@PathVariable String id,
                                     @RequestAttribute("userId") String userId) {
        return ApiResult.ok(resumeService.deleteResume(id, userId));
    }
}
