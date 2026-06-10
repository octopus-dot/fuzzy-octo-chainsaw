package com.news.service;

import com.news.common.PageResult;
import com.news.dto.FriendRequestVO;
import com.news.dto.UserVO;
import java.util.List;

public interface FriendService {
    void sendRequest(Long fromUserId, Long toUserId);
    void handleRequest(Long requestId, Long userId, String action);
    PageResult<FriendRequestVO> getReceivedRequests(Long userId, int page, int size);
    List<UserVO> getFriends(Long userId);
    boolean isFriend(Long userId, Long otherUserId);
    String getRelationStatus(Long userId, Long otherUserId);
}
