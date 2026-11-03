package com.guildnet.backend.features.like;

import com.guildnet.backend.features.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{postId}/{profileId}")
    public void like(@PathVariable Long postId, @PathVariable Long profileId, @AuthenticationPrincipal User user) {
        likeService.like(postId, profileId, user);
    }

    @DeleteMapping("/{postId}/{profileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(@PathVariable Long postId, @PathVariable Long profileId, @AuthenticationPrincipal User user) {
        likeService.unlike(postId, profileId, user);
    }

    @GetMapping("/{postId}/count")
    public int count(@PathVariable Long postId) {
        return likeService.count(postId);
    }

    @GetMapping("/{postId}/liked-by/{profileId}")
    public boolean hasLiked(@PathVariable Long postId, @PathVariable Long profileId) {
        return likeService.hasLiked(postId, profileId);
    }

    @GetMapping("/{postId}/profiles")
    public List<Long> getProfileIds(@PathVariable Long postId) {
        return likeService.getProfileIds(postId);
    }
}
