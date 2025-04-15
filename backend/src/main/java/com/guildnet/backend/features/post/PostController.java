package com.guildnet.backend.features.post;

import com.guildnet.backend.features.post.dto.PostCreateDTO;
import com.guildnet.backend.features.post.dto.PostDTO;
import com.guildnet.backend.features.post.dto.PostDetailDTO;
import com.guildnet.backend.features.post.dto.PostUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody PostCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> updatePost(@PathVariable Long id, @RequestBody PostCreateDTO dto) {
        return ResponseEntity.ok(postService.updatePost(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetailDTO> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<PostDTO>> getPostsByProfile(@PathVariable Long profileId) {
        return ResponseEntity.ok(postService.getPostsByProfileId(profileId));
    }

    @GetMapping("/community/{communityId}")
    public ResponseEntity<List<PostDTO>> getPostsByCommunity(@PathVariable Long communityId) {
        return ResponseEntity.ok(postService.getPostsByCommunityId(communityId));
    }
}

