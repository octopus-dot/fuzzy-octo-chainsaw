package com.news.service;

import com.news.common.PageResult;
import com.news.dto.PostVO;
import com.news.entity.Post;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface PostService {
    Post upload(Long userId, String title, String content, Long categoryId, List<MultipartFile> images);
    PageResult<PostVO> getUserPosts(Long userId, int page, int size);
    PageResult<PostVO> getRecommended(int page, int size);
    PageResult<PostVO> getByCategory(Long categoryId, int page, int size);
    PageResult<PostVO> search(String keyword, int page, int size);
    PostVO getDetail(Long postId, Long currentUserId);
    PageResult<PostVO> getPending(int page, int size);
    void review(Long postId, String action, String rejectReason);
    void toggleRecommend(Long postId);
    void deletePost(Long userId, Long postId);
    void adminDeletePost(Long postId);
    PageResult<PostVO> getUserApprovedPosts(Long userId, int page, int size);
}
