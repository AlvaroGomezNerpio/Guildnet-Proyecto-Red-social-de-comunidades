package com.guildnet.backend.features.community;

import com.guildnet.backend.features.community.dto.CommunityResponseDTO;
import com.guildnet.backend.features.community.dto.CreateCommunityRequest;
import com.guildnet.backend.features.community.dto.UpdateCommunityRequest;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.security.AccessGuard;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final AccessGuard accessGuard;

    @GetMapping
    public List<CommunityResponseDTO> getAll() {
        return communityService.getAll();
    }

    @GetMapping("/subscribed")
    public List<CommunityResponseDTO> getSubscribed(@AuthenticationPrincipal User user) {
        return communityService.getSubscribed(user);
    }

    @GetMapping("/suggested")
    public List<CommunityResponseDTO> getSuggested(@AuthenticationPrincipal User user) {
        return communityService.getSuggested(user);
    }

    @GetMapping("/popular")
    public List<CommunityResponseDTO> getMostPopular() {
        return communityService.getMostPopular();
    }

    @GetMapping("/search")
    public List<CommunityResponseDTO> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<String> tag
    ) {
        return communityService.search(name, tag);
    }

    @GetMapping("/{id}")
    public CommunityResponseDTO getById(@PathVariable Long id) {
        return communityService.getById(id);
    }

    /** Permisos del usuario autenticado en la comunidad, para mostrar u ocultar opciones en el frontend. */
    @GetMapping("/{id}/my-permissions")
    public List<String> getMyPermissions(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return accessGuard.permissionsOf(user, id).stream().map(Enum::name).toList();
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityResponseDTO create(
            @RequestPart("community") @Valid CreateCommunityRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "banner", required = false) MultipartFile banner,
            @AuthenticationPrincipal User user
    ) {
        return communityService.create(request, image, banner, user);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommunityResponseDTO update(
            @PathVariable Long id,
            @RequestPart("data") @Valid UpdateCommunityRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "banner", required = false) MultipartFile banner,
            @AuthenticationPrincipal User user
    ) {
        return communityService.update(id, request, image, banner, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        communityService.delete(id, user);
    }

    @DeleteMapping("/{communityId}/remove-user/{profileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(
            @PathVariable Long communityId,
            @PathVariable Long profileId,
            @AuthenticationPrincipal User user
    ) {
        communityService.removeMember(communityId, profileId, user);
    }
}
