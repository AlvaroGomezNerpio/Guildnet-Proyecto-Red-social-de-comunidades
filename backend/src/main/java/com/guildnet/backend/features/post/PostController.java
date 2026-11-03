package com.guildnet.backend.features.post;

import com.guildnet.backend.features.post.dto.PostCreateDTO;
import com.guildnet.backend.features.post.dto.PostDTO;
import com.guildnet.backend.features.post.dto.PostDetailDTO;
import com.guildnet.backend.features.post.dto.PostUpdateDTO;
import com.guildnet.backend.features.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDTO create(@RequestBody @Valid PostCreateDTO dto, @AuthenticationPrincipal User user) {
        return postService.create(dto, user);
    }

    @PutMapping("/{id}")
    public PostDTO update(@PathVariable Long id, @RequestBody @Valid PostUpdateDTO dto, @AuthenticationPrincipal User user) {
        return postService.update(id, dto, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        postService.delete(id, user);
    }

    @GetMapping("/{id}")
    public PostDetailDTO getById(@PathVariable Long id) {
        return postService.getById(id);
    }

    @GetMapping("/profile/{profileId}")
    public List<PostDTO> getByProfile(@PathVariable Long profileId) {
        return postService.getByProfile(profileId);
    }

    @GetMapping("/community/{communityId}")
    public List<PostDTO> getByCommunity(@PathVariable Long communityId) {
        return postService.getByCommunity(communityId);
    }

    @GetMapping("/search/{communityId}")
    public List<PostDTO> search(
            @PathVariable Long communityId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) List<String> tag
    ) {
        return postService.search(communityId, title, tag);
    }
}
