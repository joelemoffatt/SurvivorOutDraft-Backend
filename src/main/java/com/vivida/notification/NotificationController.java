package com.vivida.notification;

import com.vivida.auth.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationDTO> getAll(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return notificationService.getForUser(user.getId());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return Map.of("unreadCount", notificationService.getUnreadCount(user.getId()));
    }

    @PostMapping("/mark-read")
    public ResponseEntity<Void> markRead(@RequestBody MarkReadRequest request,
                                         Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        notificationService.markRead(user.getId(), request.getIds());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllRead(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        notificationService.markAllRead(user.getId());
        return ResponseEntity.noContent().build();
    }
}
