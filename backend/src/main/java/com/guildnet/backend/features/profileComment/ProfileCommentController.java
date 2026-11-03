package com.guildnet.backend.features.profileComment;

import com.guildnet.backend.features.profileComment.dto.ProfileCommentCreateUpdateDTO;
import com.guildnet.backend.features.profileComment.dto.ProfileCommentDTO;
import com.guildnet.backend.features.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profile-comments")
@RequiredArgsConstructor
public class ProfileCommentController {

    private final ProfileCommentService commentService;

    @PostMapping("/{authorProfileId}/{targetProfileId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileCommentDTO create(
            @PathVariable Long authorProfileId,
            @PathVariable Long targetProfileId,
            @RequestBody @Valid ProfileCommentCreateUpdateDTO dto,
            @AuthenticationPrincipal User user
    ) {
        return commentService.create(authorProfileId, targetProfileId, dto, user);
    }

    @PutMapping("/{commentId}")
    public ProfileCommentDTO update(@PathVariable Long commentId, @RequestBody @Valid ProfileCommentCreateUpdateDTO dto,
                                    @AuthenticationPrincipal User user) {
        return commentService.update(commentId, dto, user);
    }

    @GetMapping("/target/{targetProfileId}")
    public List<ProfileCommentDTO> getByTarget(@PathVariable Long targetProfileId) {
        return commentService.getByTarget(targetProfileId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long commentId, @AuthenticationPrincipal User user) {
        commentService.delete(commentId, user);
    }
}
