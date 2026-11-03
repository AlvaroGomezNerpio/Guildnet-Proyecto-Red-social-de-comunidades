package com.guildnet.backend.features.communityProfile;

import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.communityProfile.dto.CreateCommunityProfileRequest;
import com.guildnet.backend.features.communityProfile.dto.UpdateCommunityProfileRequest;
import com.guildnet.backend.features.role.dto.RoleDTO;
import com.guildnet.backend.features.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class CommunityProfileController {

    private final CommunityProfileService profileService;

    @GetMapping("/community/{communityId}")
    public List<CommunityProfileDTO> getByCommunity(@PathVariable Long communityId) {
        return profileService.getByCommunity(communityId);
    }

    @GetMapping("/{profileId}")
    public CommunityProfileDTO getById(@PathVariable Long profileId) {
        return profileService.getById(profileId);
    }

    @GetMapping("/me/community/{communityId}")
    public ResponseEntity<CommunityProfileDTO> getMine(@PathVariable Long communityId, @AuthenticationPrincipal User user) {
        return ResponseEntity.of(profileService.getMine(user, communityId));
    }

    /** Unirse a una comunidad indicando nombre y descripción. */
    @PostMapping("/{communityId}/create")
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityProfileDTO join(
            @PathVariable Long communityId,
            @RequestBody @Valid CreateCommunityProfileRequest request,
            @AuthenticationPrincipal User user
    ) {
        return profileService.join(user, communityId, request);
    }

    /** Unirse a una comunidad con los datos del perfil global. */
    @PostMapping("/auto-create/community/{communityId}")
    public CommunityProfileDTO autoJoin(@PathVariable Long communityId, @AuthenticationPrincipal User user) {
        return profileService.join(user, communityId, null);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommunityProfileDTO update(
            @PathVariable Long id,
            @RequestPart("data") @Valid UpdateCommunityProfileRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal User user
    ) {
        return profileService.update(id, request, image, user);
    }

    /** Abandonar la comunidad. */
    @DeleteMapping("/{id}")
    public String leave(@PathVariable Long id, @AuthenticationPrincipal User user) {
        profileService.leave(id, user);
        return "Perfil eliminado correctamente";
    }

    @PostMapping("/{profileId}/titles/{titleId}")
    public String assignTitle(@PathVariable Long profileId, @PathVariable Long titleId, @AuthenticationPrincipal User user) {
        profileService.assignTitle(profileId, titleId, user);
        return "Título asignado correctamente";
    }

    @DeleteMapping("/{profileId}/titles/{titleId}")
    public String removeTitle(@PathVariable Long profileId, @PathVariable Long titleId, @AuthenticationPrincipal User user) {
        profileService.removeTitle(profileId, titleId, user);
        return "Título eliminado correctamente";
    }

    @PutMapping("/{profileId}/featured-title/{titleId}")
    public String changeFeaturedTitle(@PathVariable Long profileId, @PathVariable Long titleId, @AuthenticationPrincipal User user) {
        profileService.changeFeaturedTitle(profileId, titleId, user);
        return "Título destacado cambiado correctamente";
    }

    @PostMapping("/{profileId}/roles/{roleId}")
    public String assignRole(@PathVariable Long profileId, @PathVariable Long roleId, @AuthenticationPrincipal User user) {
        profileService.assignRole(profileId, roleId, user);
        return "Rol asignado correctamente";
    }

    @DeleteMapping("/{profileId}/roles/{roleId}")
    public String removeRole(@PathVariable Long profileId, @PathVariable Long roleId, @AuthenticationPrincipal User user) {
        profileService.removeRole(profileId, roleId, user);
        return "Rol eliminado correctamente";
    }

    @GetMapping("/{profileId}/roles")
    public List<RoleDTO> getRoles(@PathVariable Long profileId) {
        return profileService.getRoles(profileId);
    }
}
