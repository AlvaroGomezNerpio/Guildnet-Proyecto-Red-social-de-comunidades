package com.guildnet.backend.security;

import com.guildnet.backend.common.exception.ForbiddenException;
import com.guildnet.backend.common.exception.NotFoundException;
import com.guildnet.backend.features.community.Community;
import com.guildnet.backend.features.community.CommunityRepository;
import com.guildnet.backend.features.profile.CommunityProfile;
import com.guildnet.backend.features.profile.CommunityProfileRepository;
import com.guildnet.backend.features.permission.PermissionType;
import com.guildnet.backend.features.role.RolePermissionRepository;
import com.guildnet.backend.features.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Comprobaciones de autorización de la aplicación.
 * <p>
 * Reglas:
 * <ul>
 *     <li>El creador (propietario) de una comunidad tiene todos los permisos en ella.</li>
 *     <li>El resto de miembros tienen los permisos del rol asignado a su perfil.</li>
 *     <li>Cada usuario solo puede actuar en nombre de sus propios perfiles.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccessGuard {

    private static final String NO_PERMISSION = "No tienes permiso para realizar esta acción";

    private final CommunityRepository communityRepository;
    private final CommunityProfileRepository profileRepository;
    private final RolePermissionRepository rolePermissionRepository;

    /** Perfil del usuario en la comunidad, si es miembro. */
    public Optional<CommunityProfile> findMembership(User user, Long communityId) {
        return profileRepository.findByUserIdAndCommunityId(user.getId(), communityId);
    }

    /** Devuelve el perfil del usuario en la comunidad o lanza 403 si no es miembro. */
    public CommunityProfile requireMembership(User user, Long communityId) {
        return findMembership(user, communityId)
                .orElseThrow(() -> new ForbiddenException("No perteneces a esta comunidad"));
    }

    /** Carga un perfil y comprueba que pertenece al usuario autenticado. */
    public CommunityProfile requireOwnProfile(User user, Long profileId) {
        CommunityProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new NotFoundException("Perfil no encontrado"));
        if (!isOwnProfile(user, profile)) {
            throw new ForbiddenException("Ese perfil no te pertenece");
        }
        return profile;
    }

    public boolean isOwnProfile(User user, CommunityProfile profile) {
        return profile != null && Objects.equals(profile.getUser().getId(), user.getId());
    }

    public boolean isCommunityOwner(User user, Community community) {
        return community.getOwner() != null && Objects.equals(community.getOwner().getId(), user.getId());
    }

    public void requireCommunityOwner(User user, Community community) {
        if (!isCommunityOwner(user, community)) {
            throw new ForbiddenException("Solo el creador de la comunidad puede realizar esta acción");
        }
    }

    /** ¿Tiene el usuario el permiso indicado en la comunidad? */
    public boolean hasPermission(User user, Long communityId, PermissionType permission) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NotFoundException("Comunidad no encontrada"));
        if (isCommunityOwner(user, community)) {
            return true;
        }
        return findMembership(user, communityId)
                .map(CommunityProfile::getRole)
                .map(role -> rolePermissionRepository.existsByRoleIdAndPermissionName(role.getId(), permission))
                .orElse(false);
    }

    /** Permisos efectivos del usuario en la comunidad (todos si es el creador; ninguno si no es miembro). */
    public List<PermissionType> permissionsOf(User user, Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NotFoundException("Comunidad no encontrada"));
        if (isCommunityOwner(user, community)) {
            return List.of(PermissionType.values());
        }
        return findMembership(user, communityId)
                .map(CommunityProfile::getRole)
                .map(role -> rolePermissionRepository.findByRoleId(role.getId()).stream()
                        .map(rp -> rp.getPermission().getName())
                        .toList())
                .orElse(List.of());
    }

    public void requirePermission(User user, Long communityId, PermissionType permission) {
        if (!hasPermission(user, communityId, permission)) {
            throw new ForbiddenException(NO_PERMISSION);
        }
    }

    /**
     * Permite la acción si el contenido es del propio usuario (su perfil es el autor)
     * o si tiene el permiso de moderación indicado en la comunidad del autor.
     */
    public void requireAuthorOrPermission(User user, CommunityProfile author, PermissionType permission) {
        if (isOwnProfile(user, author)) {
            return;
        }
        requirePermission(user, author.getCommunity().getId(), permission);
    }
}
