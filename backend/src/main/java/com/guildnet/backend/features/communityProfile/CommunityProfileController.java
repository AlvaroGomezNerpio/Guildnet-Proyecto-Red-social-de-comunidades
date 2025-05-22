package com.guildnet.backend.features.communityProfile;

import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.communityProfile.dto.CreateCommunityProfileRequest;
import com.guildnet.backend.features.communityProfile.dto.UpdateCommunityProfileRequest;
import com.guildnet.backend.features.role.dto.RoleDTO;
import com.guildnet.backend.features.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class CommunityProfileController {

    private final CommunityProfileService profileService;

    @GetMapping("/community/{communityId}")
    public ResponseEntity<List<CommunityProfileDTO>> getProfilesByCommunity(@PathVariable Long communityId) {
        return ResponseEntity.ok(profileService.getProfilesByCommunity(communityId));
    }

    @GetMapping("/me/community/{communityId}")
    public ResponseEntity<CommunityProfileDTO> getMyProfileInCommunity(
            @PathVariable Long communityId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return profileService.getProfile(user.getId(), communityId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{communityId}/create")
    public ResponseEntity<CommunityProfileDTO> createProfile(
            @PathVariable Long communityId,
            @RequestBody CreateCommunityProfileRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        CommunityProfileDTO profile = profileService.createProfile(request, user.getId(), communityId);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    @PostMapping("/auto-create/community/{communityId}")
    public ResponseEntity<CommunityProfileDTO> autoCreateProfile(
            @PathVariable Long communityId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal(); // obtenemos el usuario autenticado
        CommunityProfileDTO profile = profileService.createProfileAutomatically(user, communityId);
        return ResponseEntity.ok(profile);
    }


    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommunityProfileDTO> updateProfile(
            @PathVariable Long id,
            @RequestPart("data") UpdateCommunityProfileRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        CommunityProfileDTO updated = profileService.updateProfile(id, request, imageFile);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProfile(@PathVariable Long id) {
        profileService.deleteProfile(id);
        return ResponseEntity.ok("Perfil eliminado correctamente");
    }

    @PostMapping("/{profileId}/titles/{titleId}")
    public ResponseEntity<String> assignTitle(
            @PathVariable Long profileId,
            @PathVariable Long titleId
    ) {
        profileService.assignTitle(profileId, titleId);
        return ResponseEntity.ok("Título asignado correctamente");
    }

    @DeleteMapping("/{profileId}/titles/{titleId}")
    public ResponseEntity<String> removeTitle(
            @PathVariable Long profileId,
            @PathVariable Long titleId
    ) {
        profileService.removeTitle(profileId, titleId);
        return ResponseEntity.ok("Título eliminado correctamente");
    }

    @PutMapping("/{profileId}/featured-title/{titleId}")
    public ResponseEntity<String> changeFeaturedTitle(
            @PathVariable Long profileId,
            @PathVariable Long titleId
    ) {
        profileService.changeFeaturedTitle(profileId, titleId);
        return ResponseEntity.ok("Título destacado cambiado correctamente");
    }

    // Asignar un rol a un perfil
    @PostMapping("/{profileId}/roles/{roleId}")
    public ResponseEntity<String> assignRoleToProfile(
            @PathVariable Long profileId,
            @PathVariable Long roleId
    ) {
        profileService.assignRole(profileId, roleId);
        return ResponseEntity.ok("Rol asignado correctamente");
    }

    // Quitar un rol a un perfil
    @DeleteMapping("/{profileId}/roles/{roleId}")
    public ResponseEntity<String> removeRoleFromProfile(
            @PathVariable Long profileId,
            @PathVariable Long roleId
    ) {
        profileService.removeRole(profileId, roleId);
        return ResponseEntity.ok("Rol eliminado correctamente");
    }

    // Obtener todos los roles de un perfil
    @GetMapping("/{profileId}/roles")
    public ResponseEntity<List<RoleDTO>> getRolesOfProfile(
            @PathVariable Long profileId
    ) {
        List<RoleDTO> roles = profileService.getRolesByProfile(profileId);
        return ResponseEntity.ok(roles);
    }

}
