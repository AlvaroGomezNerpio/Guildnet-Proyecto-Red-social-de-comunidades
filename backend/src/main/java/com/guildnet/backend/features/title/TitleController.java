package com.guildnet.backend.features.title;

import com.guildnet.backend.features.title.dto.CreateTitleRequest;
import com.guildnet.backend.features.title.dto.TitleDTO;
import com.guildnet.backend.features.title.dto.UpdateTitleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/titles")
@RequiredArgsConstructor
public class TitleController {

    private final TitleService titleService;

    @PostMapping
    public ResponseEntity<TitleDTO> createTitle(@RequestBody CreateTitleRequest request) {
        TitleDTO created = titleService.createTitle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TitleDTO> updateTitle(
            @PathVariable Long id,
            @RequestBody UpdateTitleRequest request
    ) {
        TitleDTO updated = titleService.updateTitle(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTitle(@PathVariable Long id) {
        titleService.deleteTitle(id);
        return ResponseEntity.ok("Título eliminado correctamente");
    }

    @GetMapping("/{id}")
    public ResponseEntity<TitleDTO> getTitleById(@PathVariable Long id) {
        TitleDTO dto = titleService.getTitleById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/community/{communityId}")
    public ResponseEntity<List<TitleDTO>> getTitlesByCommunity(@PathVariable Long communityId) {
        List<TitleDTO> titles = titleService.getTitlesByCommunity(communityId);
        return ResponseEntity.ok(titles);
    }
}
