package com.guildnet.backend.features.category;

import com.guildnet.backend.features.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostCategoryController {

    private final PostCategoryService categoryService;

    @GetMapping("/communities/{communityId}/categories")
    public List<PostCategoryDTO> getByCommunity(@PathVariable Long communityId) {
        return categoryService.getByCommunity(communityId);
    }

    @PostMapping("/communities/{communityId}/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public PostCategoryDTO create(@PathVariable Long communityId, @RequestBody @Valid PostCategoryDTO dto,
                                  @AuthenticationPrincipal User user) {
        return categoryService.create(communityId, dto, user);
    }

    @PutMapping("/categories/{id}")
    public PostCategoryDTO update(@PathVariable Long id, @RequestBody @Valid PostCategoryDTO dto,
                                  @AuthenticationPrincipal User user) {
        return categoryService.update(id, dto, user);
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        categoryService.delete(id, user);
    }
}
