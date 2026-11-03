package com.guildnet.backend.features.postcomment;

import com.guildnet.backend.features.postcomment.dto.PostCommentCreateDTO;
import com.guildnet.backend.features.postcomment.dto.PostCommentDTO;
import com.guildnet.backend.features.postcomment.dto.PostCommentUpdateDTO;
import com.guildnet.backend.features.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostCommentDTO create(@RequestBody @Valid PostCommentCreateDTO dto, @AuthenticationPrincipal User user) {
        return commentService.create(dto, user);
    }

    @PutMapping("/{id}")
    public PostCommentDTO update(@PathVariable Long id, @RequestBody @Valid PostCommentUpdateDTO dto,
                                 @AuthenticationPrincipal User user) {
        return commentService.update(id, dto, user);
    }

    @GetMapping("/post/{postId}")
    public List<PostCommentDTO> getByPost(@PathVariable Long postId) {
        return commentService.getByPost(postId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long commentId, @AuthenticationPrincipal User user) {
        commentService.delete(commentId, user);
    }
}
