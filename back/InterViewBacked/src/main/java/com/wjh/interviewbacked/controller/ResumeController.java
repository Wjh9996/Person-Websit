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

    /** GET /api/resumes —— 返回整份快照 */
    @GetMapping
    public ApiResult<ResumeSnapshot> snapshot() {
        return ApiResult.ok(resumeService.getSnapshot());
    }

    /** POST /api/resumes */
    @PostMapping
    public ApiResult<ResumeNavItem> create(@Valid @RequestBody CreateResumeRequest req) {
        return ApiResult.ok(resumeService.createResume(req.getData(), req.getLabel()));
    }

    /** PUT /api/resumes/{id} */
    @PutMapping("/{id}")
    public ApiResult<Void> save(@PathVariable String id, @RequestBody ResumeData data) {
        resumeService.saveResume(id, data);
        return ApiResult.ok();
    }

    /** DELETE /api/resumes/{id} */
    @DeleteMapping("/{id}")
    public ApiResult<Boolean> delete(@PathVariable String id) {
        return ApiResult.ok(resumeService.deleteResume(id));
    }
}
