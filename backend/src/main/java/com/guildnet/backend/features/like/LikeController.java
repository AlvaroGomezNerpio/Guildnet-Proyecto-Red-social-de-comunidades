package com.guildnet.backend.features.like;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{postId}/{profileId}")
    public ResponseEntity<Void> likePost(@PathVariable Long postId, @PathVariable Long profileId) {
        likeService.likePost(postId, profileId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}/{profileId}")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId, @PathVariable Long profileId) {
        likeService.unlikePost(postId, profileId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/count")
    public ResponseEntity<Integer> countLikes(@PathVariable Long postId) {
        int count = likeService.countLikesByPostId(postId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{postId}/liked-by/{profileId}")
    public ResponseEntity<Boolean> hasUserLiked(@PathVariable Long postId, @PathVariable Long profileId) {
        boolean liked = likeService.hasUserLikedPost(postId, profileId);
        return ResponseEntity.ok(liked);
    }

    @GetMapping("/{postId}/profiles")
    public ResponseEntity<List<Long>> getProfilesWhoLiked(@PathVariable Long postId) {
        return ResponseEntity.ok(likeService.getProfileIdsWhoLikedPost(postId));
    }

}