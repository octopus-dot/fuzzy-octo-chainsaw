package com.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.news.common.PageResult;
import com.news.dto.MessageVO;
import com.news.entity.Message;
import com.news.entity.User;
import com.news.mapper.MessageMapper;
import com.news.mapper.UserMapper;
import com.news.service.FriendService;
import com.news.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final FriendService friendService;

    @Override
    @Transactional
    public MessageVO sendMessage(Long fromUserId, Long toUserId, String content) {
        if (!friendService.isFriend(fromUserId, toUserId)) {
            throw new RuntimeException("You can only send messages to friends");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Message cannot be empty");
        }
        if (content.length() > 500) {
            throw new RuntimeException("Message must not exceed 500 characters");
        }
        Message msg = new Message();
        msg.setFromUserId(fromUserId); msg.setToUserId(toUserId);
        msg.setContent(content.trim()); msg.setIsRead(false);
        messageMapper.insert(msg);
        return toVO(msg);
    }

    @Override
    public PageResult<MessageVO> getChatHistory(Long userId, Long otherUserId, int page, int size) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.and(wp -> wp.eq(Message::getFromUserId, userId).eq(Message::getToUserId, otherUserId)
             .or(wq -> wq.eq(Message::getFromUserId, otherUserId).eq(Message::getToUserId, userId)))
         .orderByDesc(Message::getCreateTime);
        Page<Message> mp = new Page<>(page, size);
        messageMapper.selectPage(mp, w);
        List<MessageVO> vos = mp.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        Collections.reverse(vos);
        return PageResult.of(mp.getTotal(), page, size, vos);
    }

    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.eq(Message::getToUserId, userId).eq(Message::getIsRead, false);
        return messageMapper.selectCount(w).intValue();
    }

    @Override
    public List<MessageVO> getConversations(Long userId) {
        // Get latest message from each conversation
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.and(wp -> wp.eq(Message::getFromUserId, userId).or(wq -> wq.eq(Message::getToUserId, userId)))
         .orderByDesc(Message::getCreateTime);
        List<Message> all = messageMapper.selectList(w);
        Map<Long, Message> latest = new LinkedHashMap<>();
        for (Message m : all) {
            Long other = m.getFromUserId().equals(userId) ? m.getToUserId() : m.getFromUserId();
            latest.putIfAbsent(other, m);
        }
        return latest.values().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long fromUserId) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.eq(Message::getToUserId, userId).eq(Message::getFromUserId, fromUserId).eq(Message::getIsRead, false);
        List<Message> unread = messageMapper.selectList(w);
        for (Message m : unread) { m.setIsRead(true); messageMapper.updateById(m); }
    }

    private MessageVO toVO(Message m) {
        MessageVO vo = new MessageVO();
        vo.setId(m.getId()); vo.setFromUserId(m.getFromUserId()); vo.setToUserId(m.getToUserId());
        vo.setContent(m.getContent()); vo.setIsRead(m.getIsRead()); vo.setCreateTime(m.getCreateTime());
        User fu = userMapper.selectById(m.getFromUserId());
        if (fu != null) { vo.setFromUserName(fu.getNickname() != null ? fu.getNickname() : fu.getUsername()); vo.setFromUserAvatar(fu.getAvatarUrl()); }
        User tu = userMapper.selectById(m.getToUserId());
        if (tu != null) { vo.setToUserName(tu.getNickname() != null ? tu.getNickname() : tu.getUsername()); vo.setToUserAvatar(tu.getAvatarUrl()); }
        return vo;
    }
}
