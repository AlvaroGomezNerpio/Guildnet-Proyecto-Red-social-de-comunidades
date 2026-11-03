package com.guildnet.backend.features.notification;

import com.guildnet.backend.features.notification.dto.NotificationDTO;
import com.guildnet.backend.features.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/profile/{profileId}")
    public List<NotificationDTO> getByProfile(@PathVariable Long profileId, @AuthenticationPrincipal User user) {
        return notificationService.getForProfile(profileId, user);
    }

    @GetMapping("/profile/{profileId}/unread/count")
    public int countUnread(@PathVariable Long profileId, @AuthenticationPrincipal User user) {
        return notificationService.countUnread(profileId, user);
    }

    @PutMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(@PathVariable Long notificationId, @AuthenticationPrincipal User user) {
        notificationService.markAsRead(notificationId, user);
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long notificationId, @AuthenticationPrincipal User user) {
        notificationService.delete(notificationId, user);
    }
}
