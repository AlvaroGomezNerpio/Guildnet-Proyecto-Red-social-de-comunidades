package com.guildnet.backend.features.post;

import com.guildnet.backend.features.post.dto.PostCreateDTO;
import com.guildnet.backend.features.post.dto.PostDTO;
import com.guildnet.backend.features.post.dto.PostDetailDTO;
import com.guildnet.backend.features.post.dto.PostUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // Crear un nuevo post
    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody PostCreateDTO postCreateDTO) {
        PostDTO created = postService.createPost(postCreateDTO);
        return ResponseEntity.status(201).body(created);
    }

    // Obtener todos los posts
    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    // Obtener un post por su ID
    @GetMapping("/{id}")
    public ResponseEntity<PostDetailDTO> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    // Actualizar un post (solo título, contenido y tags)
    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> updatePost(@PathVariable Long id, @RequestBody PostUpdateDTO updatedPost) {
        return ResponseEntity.ok(postService.updatePost(id, updatedPost));
    }

    // Eliminar un post y sus comentarios
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
