package com.news.controller;

import com.news.common.PageResult;
import com.news.common.Result;
import com.news.dto.ReportVO;
import com.news.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/report/{postId}")
    public Result<Void> report(@PathVariable Long postId, @RequestBody Map<String, String> body,
                                Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        reportService.reportPost(userId, postId, body.get("reason"), body.get("description"));
        return Result.ok();
    }

    @DeleteMapping("/report/{postId}")
    public Result<Void> cancelReport(@PathVariable Long postId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        reportService.cancelReport(userId, postId);
        return Result.ok();
    }

    @GetMapping("/user/reports")
    public Result<PageResult<ReportVO>> getUserReports(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(reportService.getUserReports(userId, page, size));
    }

    @GetMapping("/admin/reports/pending")
    public Result<PageResult<ReportVO>> getPendingReports(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return Result.ok(reportService.getPendingReports(page, size));
    }

    @PostMapping("/admin/reports/handle")
    public Result<Void> handleReport(@RequestBody Map<String, Object> body, Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        reportService.handleReport(
            Long.valueOf(body.get("reportId").toString()),
            (String) body.get("action"),
            adminId,
            (String) body.get("handleNote")
        );
        return Result.ok();
    }
}
