package com.guildnet.backend.features.postComment;

import com.guildnet.backend.features.postComment.dto.PostComentDTO;
import com.guildnet.backend.features.postComment.dto.PostCommentCreateDTO;
import com.guildnet.backend.features.postComment.dto.PostCommentUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentService postCommentService;

    // Crear un comentario
    @PostMapping
    public ResponseEntity<PostComentDTO> createComment(@RequestBody PostCommentCreateDTO dto) {
        return ResponseEntity.ok(postCommentService.createComment(dto));
    }

    // Actualizar un comentario
    @PutMapping("/{id}")
    public ResponseEntity<PostComentDTO> updateComment(@PathVariable Long id,
                                                       @RequestBody PostCommentUpdateDTO dto) {
        PostComentDTO updated = postCommentService.updateComment(id, dto);
        return ResponseEntity.ok(updated);
    }

    // Obtener comentarios por ID de post
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<PostComentDTO>> getCommentsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postCommentService.getCommentsByPostId(postId));
    }

    // Eliminar comentario por ID
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        postCommentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
