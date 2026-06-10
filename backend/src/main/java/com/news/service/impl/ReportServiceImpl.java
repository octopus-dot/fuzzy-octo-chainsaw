package com.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.news.common.PageResult;
import com.news.dto.ReportVO;
import com.news.entity.Post;
import com.news.entity.Report;
import com.news.entity.User;
import com.news.mapper.*;
import com.news.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void reportPost(Long reporterId, Long postId, String reason, String description) {
        Post post = postMapper.selectById(postId);
        if (post == null || !"approved".equals(post.getStatus())) {
            throw new RuntimeException("Post not found or not approved for reporting");
        }
        if (post.getUserId().equals(reporterId)) {
            throw new RuntimeException("You cannot report your own post");
        }
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getReporterId, reporterId).eq(Report::getPostId, postId);
        if (reportMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("You have already reported this post");
        }
        Report report = new Report();
        report.setPostId(postId);
        report.setReporterId(reporterId);
        report.setReason(reason);
        report.setDescription(description);
        report.setStatus("pending");
        reportMapper.insert(report);
    }

    @Override
    @Transactional
    public void cancelReport(Long reporterId, Long postId) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getReporterId, reporterId).eq(Report::getPostId, postId);
        reportMapper.delete(wrapper);
    }

    @Override
    public PageResult<ReportVO> getPendingReports(int page, int size) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getStatus, "pending").orderByAsc(Report::getCreateTime);
        Page<Report> rp = new Page<>(page, size);
        reportMapper.selectPage(rp, wrapper);
        List<ReportVO> vos = rp.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(rp.getTotal(), page, size, vos);
    }

    @Override
    public PageResult<ReportVO> getUserReports(Long userId, int page, int size) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getReporterId, userId).orderByDesc(Report::getCreateTime);
        Page<Report> rp = new Page<>(page, size);
        reportMapper.selectPage(rp, wrapper);
        List<ReportVO> vos = rp.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(rp.getTotal(), page, size, vos);
    }

    @Override
    @Transactional
    public void handleReport(Long reportId, String action, Long adminId, String handleNote) {
        Report report = reportMapper.selectById(reportId);
        if (report == null || !"pending".equals(report.getStatus())) {
            throw new RuntimeException("Report not found or already handled");
        }
        if ("confirm".equals(action)) {
            report.setStatus("handled");
            report.setAdminId(adminId);
            report.setHandleNote(handleNote);
            reportMapper.updateById(report);
            // Delete the reported post
            Post post = postMapper.selectById(report.getPostId());
            if (post != null) postMapper.deleteById(post.getId());
        } else if ("dismiss".equals(action)) {
            report.setStatus("dismissed");
            report.setAdminId(adminId);
            report.setHandleNote(handleNote);
            reportMapper.updateById(report);
        } else {
            throw new RuntimeException("Invalid action: confirm or dismiss");
        }
    }

    private ReportVO toVO(Report r) {
        ReportVO vo = new ReportVO();
        vo.setId(r.getId()); vo.setPostId(r.getPostId()); vo.setReporterId(r.getReporterId());
        vo.setReason(r.getReason()); vo.setDescription(r.getDescription());
        vo.setStatus(r.getStatus()); vo.setAdminId(r.getAdminId());
        vo.setHandleNote(r.getHandleNote()); vo.setCreateTime(r.getCreateTime());
        Post post = postMapper.selectById(r.getPostId());
        if (post != null) vo.setPostTitle(post.getTitle());
        User user = userMapper.selectById(r.getReporterId());
        if (user != null) vo.setReporterName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        return vo;
    }
}
