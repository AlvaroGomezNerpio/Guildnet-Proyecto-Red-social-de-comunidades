package com.guildnet.backend.features.Community;

import com.guildnet.backend.features.Community.dto.CommunityResponse;
import com.guildnet.backend.features.Community.dto.CommunityResponseDTO;
import com.guildnet.backend.features.Community.dto.CreateCommunityRequest;
import com.guildnet.backend.features.Community.dto.UpdateCommunityRequest;
import com.guildnet.backend.features.communityProfile.CommunityProfileService;
import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.permission.PermissionService;
import com.guildnet.backend.features.permission.dto.PermissionDTO;
import com.guildnet.backend.features.role.RoleService;
import com.guildnet.backend.features.role.dto.RoleDTO;
import com.guildnet.backend.features.rolePermission.RolePermissionService;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.features.user.UserService;
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
@RequestMapping("/api/v1/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final CommunityProfileService profileService;
    private final RolePermissionService rolePermissionService;

    @GetMapping
    public ResponseEntity<List<CommunityResponseDTO>> getAll() {
        List<CommunityResponseDTO> response = communityService.getAllCommunities().stream()
                .map(c -> new CommunityResponseDTO(
                        c.getId(),
                        c.getName(),
                        c.getDescription(),
                        c.getRules(),
                        c.getImage(),
                        c.getBanner(),
                        c.getTags()
                ))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subscribed")
    public ResponseEntity<List<CommunityResponseDTO>> getSubscribedCommunities(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Community> subscribed = communityService.getCommunitiesByUser(user);

        List<CommunityResponseDTO> response = subscribed.stream()
                .map(c -> new CommunityResponseDTO(
                        c.getId(),
                        c.getName(),
                        c.getDescription(),
                        c.getRules(),
                        c.getImage(),
                        c.getBanner(),
                        c.getTags()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggested")
    public ResponseEntity<List<CommunityResponseDTO>> getSuggestedCommunities(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Community> suggested = communityService.getSuggestedCommunities(user);

        List<CommunityResponseDTO> response = suggested.stream()
                .map(c -> new CommunityResponseDTO(
                        c.getId(),
                        c.getName(),
                        c.getDescription(),
                        c.getRules(),
                        c.getImage(),
                        c.getBanner(),
                        c.getTags()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<CommunityResponseDTO>> getMostPopularCommunities() {
        List<Community> popular = communityService.getMostPopularCommunities();

        List<CommunityResponseDTO> response = popular.stream()
                .map(c -> new CommunityResponseDTO(
                        c.getId(),
                        c.getName(),
                        c.getDescription(),
                        c.getRules(),
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
                        c.getRules(),
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
                        c.getRules(),
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

        // Paso 1: Crear comunidad
        CommunityResponse response = communityService.createCommunity(request, owner, imageFile, bannerFile);

        // Paso 2: Crear perfil en la comunidad
        CommunityProfileDTO profile = profileService.createProfileAutomatically(owner, response.getId());

        // Paso 3: Crear rol "Líder" para esta comunidad
        RoleDTO leaderRole = roleService.createRole(
                RoleDTO.builder()
                        .name("Líder")
                        .textColor("#ffffff")
                        .backgroundColor("#000000")
                        .communityId(response.getId())
                        .build()
        );

        // Paso 4: Asignar todos los permisos al rol "Líder"
        List<PermissionDTO> allPermissions = permissionService.getAllPermissions();
        for (PermissionDTO permission : allPermissions) {
            rolePermissionService.assignPermissionToRole(leaderRole.getId(), permission.getId());
        }

        // Paso 5: Asignar el rol al perfil creado
        profileService.assignRole(profile.getId(), leaderRole.getId());

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

    @DeleteMapping("/{communityId}/remove-user/{profileId}")
    public ResponseEntity<?> removeUserFromCommunity(
            @PathVariable Long communityId,
            @PathVariable Long profileId,
            Authentication authentication
    ) {
        User requester = (User) authentication.getPrincipal();

        // Validación opcional: verificar que el requester tiene permisos (por implementar si quieres)
        profileService.removeProfileFromCommunity(profileId, communityId);

        return ResponseEntity.ok("Usuario eliminado de la comunidad");
    }

}



