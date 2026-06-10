package com.news.controller;

import com.news.common.PageResult;
import com.news.common.Result;
import com.news.dto.FriendRequestVO;
import com.news.dto.UserVO;
import com.news.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/friend/request/{toUserId}")
    public Result<Void> sendRequest(@PathVariable Long toUserId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        friendService.sendRequest(userId, toUserId);
        return Result.ok();
    }

    @PutMapping("/friend/request/{requestId}")
    public Result<Void> handleRequest(@PathVariable Long requestId,
                                       @RequestBody Map<String, String> body,
                                       Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        friendService.handleRequest(requestId, userId, body.get("action"));
        return Result.ok();
    }

    @GetMapping("/friend/requests")
    public Result<PageResult<FriendRequestVO>> getRequests(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(friendService.getReceivedRequests(userId, page, size));
    }

    @GetMapping("/friends")
    public Result<List<UserVO>> getFriends(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(friendService.getFriends(userId));
    }

    @GetMapping("/friend/status/{otherUserId}")
    public Result<Map<String, String>> getRelationStatus(@PathVariable Long otherUserId,
                                                          Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        return Result.ok(Map.of("status", friendService.getRelationStatus(userId, otherUserId)));
    }
}
