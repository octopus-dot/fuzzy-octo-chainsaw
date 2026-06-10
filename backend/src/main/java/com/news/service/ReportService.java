package com.news.service;

import com.news.common.PageResult;
import com.news.dto.ReportVO;

public interface ReportService {
    void reportPost(Long reporterId, Long postId, String reason, String description);
    void cancelReport(Long reporterId, Long postId);
    PageResult<ReportVO> getPendingReports(int page, int size);
    PageResult<ReportVO> getUserReports(Long userId, int page, int size);
    void handleReport(Long reportId, String action, Long adminId, String handleNote);
}
