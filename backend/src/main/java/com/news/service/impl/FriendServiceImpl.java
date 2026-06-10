package com.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.news.common.PageResult;
import com.news.dto.FriendRequestVO;
import com.news.dto.UserVO;
import com.news.entity.FriendRequest;
import com.news.entity.Friendship;
import com.news.entity.User;
import com.news.mapper.FriendRequestMapper;
import com.news.mapper.FriendshipMapper;
import com.news.mapper.UserMapper;
import com.news.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendRequestMapper requestMapper;
    private final FriendshipMapper friendshipMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void sendRequest(Long fromUserId, Long toUserId) {
        if (fromUserId.equals(toUserId)) throw new RuntimeException("Cannot add yourself as friend");
        if (isFriend(fromUserId, toUserId)) throw new RuntimeException("Already friends");
        LambdaQueryWrapper<FriendRequest> w = new LambdaQueryWrapper<>();
        w.eq(FriendRequest::getFromUserId, fromUserId).eq(FriendRequest::getToUserId, toUserId)
         .eq(FriendRequest::getStatus, "pending");
        if (requestMapper.selectCount(w) > 0) throw new RuntimeException("Friend request already sent");
        FriendRequest req = new FriendRequest();
        req.setFromUserId(fromUserId); req.setToUserId(toUserId); req.setStatus("pending");
        requestMapper.insert(req);
    }

    @Override
    @Transactional
    public void handleRequest(Long requestId, Long userId, String action) {
        FriendRequest req = requestMapper.selectById(requestId);
        if (req == null || !req.getToUserId().equals(userId)) throw new RuntimeException("Request not found");
        if (!"pending".equals(req.getStatus())) throw new RuntimeException("Request already handled");
        if ("accept".equals(action)) {
            req.setStatus("accepted"); requestMapper.updateById(req);
            Friendship f1 = new Friendship(); f1.setUserId(req.getFromUserId()); f1.setFriendId(req.getToUserId());
            Friendship f2 = new Friendship(); f2.setUserId(req.getToUserId()); f2.setFriendId(req.getFromUserId());
            friendshipMapper.insert(f1); friendshipMapper.insert(f2);
        } else if ("reject".equals(action)) {
            req.setStatus("rejected"); requestMapper.updateById(req);
        }
    }

    @Override
    public PageResult<FriendRequestVO> getReceivedRequests(Long userId, int page, int size) {
        LambdaQueryWrapper<FriendRequest> w = new LambdaQueryWrapper<>();
        w.eq(FriendRequest::getToUserId, userId).eq(FriendRequest::getStatus, "pending")
         .orderByDesc(FriendRequest::getCreateTime);
        Page<FriendRequest> rp = new Page<>(page, size);
        requestMapper.selectPage(rp, w);
        List<FriendRequestVO> vos = rp.getRecords().stream().map(r -> {
            FriendRequestVO vo = new FriendRequestVO();
            vo.setId(r.getId()); vo.setFromUserId(r.getFromUserId());
            vo.setToUserId(r.getToUserId()); vo.setStatus(r.getStatus());
            vo.setCreateTime(r.getCreateTime());
            User u = userMapper.selectById(r.getFromUserId());
            if (u != null) { vo.setFromUserName(u.getNickname() != null ? u.getNickname() : u.getUsername()); vo.setFromUserAvatar(u.getAvatarUrl()); }
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(rp.getTotal(), page, size, vos);
    }

    @Override
    public List<UserVO> getFriends(Long userId) {
        LambdaQueryWrapper<Friendship> w = new LambdaQueryWrapper<>();
        w.eq(Friendship::getUserId, userId);
        return friendshipMapper.selectList(w).stream().map(f -> {
            User u = userMapper.selectById(f.getFriendId());
            if (u == null) return null;
            UserVO vo = new UserVO();
            vo.setId(u.getId()); vo.setUsername(u.getUsername()); vo.setNickname(u.getNickname());
            vo.setAvatarUrl(u.getAvatarUrl()); vo.setBio(u.getBio()); vo.setBackgroundUrl(u.getBackgroundUrl());
            return vo;
        }).filter(v -> v != null).collect(Collectors.toList());
    }

    @Override
    public boolean isFriend(Long userId, Long otherUserId) {
        LambdaQueryWrapper<Friendship> w = new LambdaQueryWrapper<>();
        w.eq(Friendship::getUserId, userId).eq(Friendship::getFriendId, otherUserId);
        return friendshipMapper.selectCount(w) > 0;
    }

    @Override
    public String getRelationStatus(Long userId, Long otherUserId) {
        if (userId == null || userId.equals(otherUserId)) return "self";
        if (isFriend(userId, otherUserId)) return "friend";
        LambdaQueryWrapper<FriendRequest> w = new LambdaQueryWrapper<>();
        w.eq(FriendRequest::getFromUserId, userId).eq(FriendRequest::getToUserId, otherUserId)
         .eq(FriendRequest::getStatus, "pending");
        if (requestMapper.selectCount(w) > 0) return "request_sent";
        return "none";
    }
}
