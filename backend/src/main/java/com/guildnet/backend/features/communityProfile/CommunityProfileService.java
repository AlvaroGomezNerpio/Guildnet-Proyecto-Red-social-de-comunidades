package com.guildnet.backend.features.communityProfile;

import com.guildnet.backend.common.exception.BadRequestException;
import com.guildnet.backend.common.exception.ConflictException;
import com.guildnet.backend.common.exception.ForbiddenException;
import com.guildnet.backend.common.exception.NotFoundException;
import com.guildnet.backend.common.html.HtmlSanitizer;
import com.guildnet.backend.common.mapper.DtoMapper;
import com.guildnet.backend.features.community.Community;
import com.guildnet.backend.features.community.CommunityRepository;
import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.communityProfile.dto.CreateCommunityProfileRequest;
import com.guildnet.backend.features.communityProfile.dto.UpdateCommunityProfileRequest;
import com.guildnet.backend.features.notification.NotificationService;
import com.guildnet.backend.features.notification.NotificationType;
import com.guildnet.backend.features.permission.PermissionType;
import com.guildnet.backend.features.role.Role;
import com.guildnet.backend.features.role.RoleRepository;
import com.guildnet.backend.features.role.dto.RoleDTO;
import com.guildnet.backend.features.title.Title;
import com.guildnet.backend.features.title.TitleRepository;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.features.user.UserRepository;
import com.guildnet.backend.security.AccessGuard;
import com.guildnet.backend.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityProfileService {

    private final CommunityProfileRepository profileRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final TitleRepository titleRepository;
    private final RoleRepository roleRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final AccessGuard accessGuard;

    // ------------------------------------------------------------------ Consultas

    @Transactional(readOnly = true)
    public List<CommunityProfileDTO> getByCommunity(Long communityId) {
        return profileRepository.findByCommunityId(communityId).stream().map(DtoMapper::toProfileDTO).toList();
    }

    @Transactional(readOnly = true)
    public CommunityProfileDTO getById(Long profileId) {
        return DtoMapper.toProfileDTO(load(profileId));
    }

    @Transactional(readOnly = true)
    public Optional<CommunityProfileDTO> getMine(User user, Long communityId) {
        return accessGuard.findMembership(user, communityId).map(DtoMapper::toProfileDTO);
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> getRoles(Long profileId) {
        Role role = load(profileId).getRole();
        return role == null ? List.of() : List.of(DtoMapper.toRoleDTO(role));
    }

    // ------------------------------------------------------------------ Unirse / salir

    /** El usuario autenticado se une a la comunidad. */
    public CommunityProfileDTO join(User authUser, Long communityId, CreateCommunityProfileRequest request) {
        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NotFoundException("Comunidad no encontrada"));

        String username = request != null ? request.username() : null;
        String description = request != null ? request.description() : null;
        CommunityProfile profile = createMembership(user, community, username, description);

        notificationService.notify(profile, null, NotificationType.WELCOME,
                "¡Bienvenido a " + community.getName() + "!");
        return DtoMapper.toProfileDTO(profile);
    }

    /**
     * Crea el perfil del usuario dentro de la comunidad. Por defecto usa el nombre
     * y una copia de la imagen de su perfil global.
     */
    public CommunityProfile createMembership(User user, Community community, String username, String description) {
        if (profileRepository.existsByUserIdAndCommunityId(user.getId(), community.getId())) {
            throw new ConflictException("Ya perteneces a esta comunidad");
        }
        CommunityProfile profile = CommunityProfile.builder()
                .username(username != null && !username.isBlank() ? username.trim() : user.getDisplayName())
                .description(HtmlSanitizer.sanitize(description))
                .profileImage(fileStorageService.copyImage(user.getProfileImage(), FileStorageService.Folder.COMMUNITY_PROFILES))
                .user(user)
                .community(community)
                .build();
        return profileRepository.save(profile);
    }

    /** Abandonar la comunidad (borrar el propio perfil). El creador no puede abandonarla, debe borrarla. */
    public void leave(Long profileId, User user) {
        CommunityProfile profile = accessGuard.requireOwnProfile(user, profileId);
        if (accessGuard.isCommunityOwner(user, profile.getCommunity())) {
            throw new ForbiddenException("El creador no puede abandonar su comunidad; puede borrarla");
        }
        profileRepository.delete(profile);
    }

    // ------------------------------------------------------------------ Edición del propio perfil

    public CommunityProfileDTO update(Long profileId, UpdateCommunityProfileRequest request, MultipartFile image, User user) {
        CommunityProfile profile = accessGuard.requireOwnProfile(user, profileId);

        if (request.description() != null) {
            profile.setDescription(HtmlSanitizer.sanitize(request.description()));
        }
        if (request.userName() != null && !request.userName().isBlank()) {
            profile.setUsername(request.userName().trim());
        }
        String imageUrl = fileStorageService.storeImage(image, FileStorageService.Folder.COMMUNITY_PROFILES);
        if (imageUrl != null) {
            profile.setProfileImage(imageUrl);
        }
        return DtoMapper.toProfileDTO(profile);
    }

    public void changeFeaturedTitle(Long profileId, Long titleId, User user) {
        CommunityProfile profile = accessGuard.requireOwnProfile(user, profileId);
        Title title = loadTitle(titleId);
        if (!profile.getTitles().contains(title)) {
            throw new BadRequestException("El título no está asignado al perfil");
        }
        profile.setFeaturedTitle(title);
    }

    // ------------------------------------------------------------------ Títulos (requiere ASSIGN_TITLES)

    public void assignTitle(Long profileId, Long titleId, User user) {
        CommunityProfile profile = load(profileId);
        Title title = loadTitle(titleId);
        requireSameCommunity(profile, title.getCommunity());
        accessGuard.requirePermission(user, profile.getCommunity().getId(), PermissionType.ASSIGN_TITLES);

        if (!profile.getTitles().contains(title)) {
            profile.getTitles().add(title);
            notificationService.notify(profile, actorProfile(user, profile), NotificationType.TITLE_ASSIGNED,
                    "Te ha asignado el título \"" + title.getTitle() + "\".");
        }
    }

    public void removeTitle(Long profileId, Long titleId, User user) {
        CommunityProfile profile = load(profileId);
        Title title = loadTitle(titleId);
        requireSameCommunity(profile, title.getCommunity());
        accessGuard.requirePermission(user, profile.getCommunity().getId(), PermissionType.ASSIGN_TITLES);

        if (profile.getTitles().remove(title)) {
            if (title.equals(profile.getFeaturedTitle())) {
                profile.setFeaturedTitle(null);
            }
            notificationService.notify(profile, actorProfile(user, profile), NotificationType.TITLE_REMOVED,
                    "Te ha eliminado el título \"" + title.getTitle() + "\".");
        }
    }

    // ------------------------------------------------------------------ Roles (requiere ASSIGN_ROLES)

    public void assignRole(Long profileId, Long roleId, User user) {
        CommunityProfile profile = load(profileId);
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new NotFoundException("Rol no encontrado"));
        requireSameCommunity(profile, role.getCommunity());
        requireCanManageRoleOf(user, profile);

        profile.setRole(role);
        notificationService.notify(profile, actorProfile(user, profile), NotificationType.ROLE_ASSIGNED,
                "Te ha asignado el rol \"" + role.getName() + "\".");
    }

    public void removeRole(Long profileId, Long roleId, User user) {
        CommunityProfile profile = load(profileId);
        requireCanManageRoleOf(user, profile);

        Role current = profile.getRole();
        if (current != null && current.getId().equals(roleId)) {
            profile.setRole(null);
            notificationService.notify(profile, actorProfile(user, profile), NotificationType.ROLE_REMOVED,
                    "Te ha eliminado el rol \"" + current.getName() + "\".");
        }
    }

    /**
     * Para cambiar roles hace falta ASSIGN_ROLES. Además, para evitar escalada de privilegios,
     * nadie salvo el creador puede cambiar su propio rol ni el rol del creador.
     */
    private void requireCanManageRoleOf(User user, CommunityProfile target) {
        Community community = target.getCommunity();
        accessGuard.requirePermission(user, community.getId(), PermissionType.ASSIGN_ROLES);
        if (accessGuard.isCommunityOwner(user, community)) {
            return;
        }
        if (accessGuard.isOwnProfile(user, target) || accessGuard.isCommunityOwner(target.getUser(), community)) {
            throw new ForbiddenException("No puedes cambiar tu propio rol ni el del creador de la comunidad");
        }
    }

    // ------------------------------------------------------------------ Utilidades

    private CommunityProfile actorProfile(User user, CommunityProfile target) {
        return accessGuard.findMembership(user, target.getCommunity().getId()).orElse(null);
    }

    private static void requireSameCommunity(CommunityProfile profile, Community community) {
        if (!profile.getCommunity().getId().equals(community.getId())) {
            throw new BadRequestException("El perfil y el elemento pertenecen a comunidades distintas");
        }
    }

    private CommunityProfile load(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new NotFoundException("Perfil no encontrado"));
    }

    private Title loadTitle(Long titleId) {
        return titleRepository.findById(titleId)
                .orElseThrow(() -> new NotFoundException("Título no encontrado"));
    }
}
