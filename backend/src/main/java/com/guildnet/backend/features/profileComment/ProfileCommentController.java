package com.guildnet.backend.features.profileComment;

import com.guildnet.backend.features.profileComment.dto.ProfileCommentCreateUpdateDTO;
import com.guildnet.backend.features.profileComment.dto.ProfileCommentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile-comments")
@RequiredArgsConstructor
public class ProfileCommentController {

    private final ProfileCommentService profileCommentService;

    @PostMapping("/{authorProfileId}/{targetProfileId}")
    public ResponseEntity<ProfileCommentDTO> createComment(
            @PathVariable Long authorProfileId,
            @PathVariable Long targetProfileId,
            @RequestBody ProfileCommentCreateUpdateDTO dto
    ) {
        return new ResponseEntity<>(
                profileCommentService.createProfileComment(authorProfileId, targetProfileId, dto),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ProfileCommentDTO> updateComment(
            @PathVariable Long commentId,
            @RequestBody ProfileCommentCreateUpdateDTO dto) {
        return ResponseEntity.ok(profileCommentService.updateProfileComment(commentId, dto));
    }

    @GetMapping("/target/{targetProfileId}")
    public ResponseEntity<List<ProfileCommentDTO>> getCommentsByTarget(
            @PathVariable Long targetProfileId) {
        return ResponseEntity.ok(profileCommentService.getCommentsByTargetProfileId(targetProfileId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        profileCommentService.deleteProfileComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
