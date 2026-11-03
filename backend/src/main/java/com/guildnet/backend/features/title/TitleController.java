package com.guildnet.backend.features.title;

import com.guildnet.backend.features.title.dto.CreateTitleRequest;
import com.guildnet.backend.features.title.dto.TitleDTO;
import com.guildnet.backend.features.title.dto.UpdateTitleRequest;
import com.guildnet.backend.features.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/titles")
@RequiredArgsConstructor
public class TitleController {

    private final TitleService titleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TitleDTO create(@RequestBody @Valid CreateTitleRequest request, @AuthenticationPrincipal User user) {
        return titleService.create(request, user);
    }

    @PutMapping("/{id}")
    public TitleDTO update(@PathVariable Long id, @RequestBody @Valid UpdateTitleRequest request,
                           @AuthenticationPrincipal User user) {
        return titleService.update(id, request, user);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        titleService.delete(id, user);
        return "Título eliminado correctamente";
    }

    @GetMapping("/{id}")
    public TitleDTO getById(@PathVariable Long id) {
        return titleService.getById(id);
    }

    @GetMapping("/community/{communityId}")
    public List<TitleDTO> getByCommunity(@PathVariable Long communityId) {
        return titleService.getByCommunity(communityId);
    }
}
