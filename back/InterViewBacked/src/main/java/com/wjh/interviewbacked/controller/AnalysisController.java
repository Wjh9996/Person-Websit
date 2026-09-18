package com.wjh.interviewbacked.controller;

import com.wjh.interviewbacked.common.ApiResult;
import com.wjh.interviewbacked.dto.AnalysisAggregate;
import com.wjh.interviewbacked.dto.AnalysisTriggerResult;
import com.wjh.interviewbacked.dto.TaskStatusVO;
import com.wjh.interviewbacked.service.AnalysisService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /** POST /api/notes/{id}/analyze?types=summary,keywords 需登录 + 归属 */
    @PostMapping("/notes/{id}/analyze")
    public ApiResult<AnalysisTriggerResult> analyze(@PathVariable String id,
                                                     @RequestParam(required = false) List<String> types,
                                                     @RequestAttribute(value = "userId", required = false) String userId) {
        if (userId == null) return ApiResult.error(401, "未登录或缺少令牌");
        return ApiResult.ok(new AnalysisTriggerResult(analysisService.triggerAnalysis(id, userId, types)));
    }

    /** GET /api/notes/{id}/analysis 需登录 + 归属 */
    @GetMapping("/notes/{id}/analysis")
    public ApiResult<AnalysisAggregate> analysis(@PathVariable String id,
                                                  @RequestAttribute(value = "userId", required = false) String userId) {
        if (userId == null) return ApiResult.error(401, "未登录或缺少令牌");
        return ApiResult.ok(analysisService.getAnalysis(id, userId));
    }

    /** GET /api/analysis/tasks/{taskId} 需登录 */
    @GetMapping("/analysis/tasks/{taskId}")
    public ApiResult<TaskStatusVO> task(@PathVariable String taskId,
                                         @RequestAttribute(value = "userId", required = false) String userId) {
        if (userId == null) return ApiResult.error(401, "未登录或缺少令牌");
        return ApiResult.ok(analysisService.getTaskStatus(taskId, userId));
    }
}
