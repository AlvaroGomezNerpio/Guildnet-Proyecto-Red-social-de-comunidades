package com.guildnet.backend.features.notification;

import com.guildnet.backend.features.notification.dto.NotificationCreateDTO;
import com.guildnet.backend.features.notification.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByProfile(@PathVariable Long profileId) {
        return ResponseEntity.ok(notificationService.getNotificationsForProfile(profileId));
    }

    @GetMapping("/profile/{profileId}/unread/count")
    public ResponseEntity<Integer> countUnreadNotifications(@PathVariable Long profileId) {
        int count = notificationService.countUnreadNotifications(profileId);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<NotificationDTO> createNotification(@RequestBody NotificationCreateDTO dto) {
        NotificationDTO created = notificationService.createNotification(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}


