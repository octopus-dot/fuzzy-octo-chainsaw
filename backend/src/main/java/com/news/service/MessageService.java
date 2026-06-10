package com.news.service;

import com.news.common.PageResult;
import com.news.dto.MessageVO;
import java.util.List;

public interface MessageService {
    MessageVO sendMessage(Long fromUserId, Long toUserId, String content);
    PageResult<MessageVO> getChatHistory(Long userId, Long otherUserId, int page, int size);
    int getUnreadCount(Long userId);
    List<MessageVO> getConversations(Long userId);
    void markAsRead(Long userId, Long fromUserId);
}
