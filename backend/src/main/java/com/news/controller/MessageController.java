package com.news.controller;

import com.news.common.PageResult;
import com.news.common.Result;
import com.news.dto.MessageVO;
import com.news.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/messages")
    public Result<MessageVO> sendMessage(@RequestBody Map<String, String> body,
                                          Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Long toUserId = Long.valueOf(body.get("toUserId"));
        return Result.ok(messageService.sendMessage(userId, toUserId, body.get("content")));
    }

    @GetMapping("/messages/chat/{otherUserId}")
    public Result<PageResult<MessageVO>> getChatHistory(
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(messageService.getChatHistory(userId, otherUserId, page, size));
    }

    @GetMapping("/messages/unread")
    public Result<Map<String, Integer>> getUnreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(Map.of("count", messageService.getUnreadCount(userId)));
    }

    @GetMapping("/messages/conversations")
    public Result<List<MessageVO>> getConversations(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(messageService.getConversations(userId));
    }

    @PutMapping("/messages/read/{fromUserId}")
    public Result<Void> markAsRead(@PathVariable Long fromUserId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        messageService.markAsRead(userId, fromUserId);
        return Result.ok();
    }
}
