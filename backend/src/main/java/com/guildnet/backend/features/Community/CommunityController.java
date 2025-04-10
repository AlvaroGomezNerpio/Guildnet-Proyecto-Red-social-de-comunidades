package com.guildnet.backend.features.Community;

import com.guildnet.backend.features.Community.dto.CommunityResponse;
import com.guildnet.backend.features.Community.dto.CommunityResponseDTO;
import com.guildnet.backend.features.Community.dto.CreateCommunityRequest;
import com.guildnet.backend.features.Community.dto.UpdateCommunityRequest;
import com.guildnet.backend.features.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping
    public ResponseEntity<List<CommunityResponseDTO>> getAll() {
        List<CommunityResponseDTO> response = communityService.getAllCommunities().stream()
                .map(c -> new CommunityResponseDTO(
                        c.getId(),
                        c.getName(),
                        c.getDescription(),
                        c.getImage(),
                        c.getBanner(),
                        c.getTags()
                ))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCommunityById(@PathVariable Long id) {
        return communityService.getCommunityById(id)
                .map(c -> new CommunityResponseDTO(
                        c.getId(),
                        c.getName(),
                        c.getDescription(),
                        c.getImage(),
                        c.getBanner(),
                        c.getTags()
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<CommunityResponseDTO>> searchByNameAndTag(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<String> tag
    ) {
        List<Community> results;

        if (name != null && tag != null && !tag.isEmpty()) {
            results = communityService.searchByNameAndTags(name, tag);
        } else if (name != null) {
            results = communityService.searchCommunitiesByName(name);
        } else if (tag != null && !tag.isEmpty()) {
            results = communityService.searchByTags(tag);
        } else {
            results = communityService.getAllCommunities();
        }

        List<CommunityResponseDTO> response = results.stream()
                .map(c -> new CommunityResponseDTO(
                        c.getId(),
                        c.getName(),
                        c.getDescription(),
                        c.getImage(),
                        c.getBanner(),
                        c.getTags()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CommunityResponse> createCommunity(
            @RequestPart("community") CreateCommunityRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestPart(value = "banner", required = false) MultipartFile bannerFile,
            Authentication authentication
    ) {
        User owner = (User) authentication.getPrincipal();
        CommunityResponse response = communityService.createCommunity(request, owner, imageFile, bannerFile);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> updateCommunity(
            @PathVariable Long id,
            @RequestPart("data") UpdateCommunityRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestPart(value = "banner", required = false) MultipartFile bannerFile
    ) {
        return communityService.updateCommunity(id, request, imageFile, bannerFile)
                .map(updated -> ResponseEntity.ok("Comunidad actualizada correctamente"))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCommunity(@PathVariable Long id) {
        communityService.deleteCommunity(id);
        return ResponseEntity.ok("Comunidad eliminada correctamente");
    }
}



