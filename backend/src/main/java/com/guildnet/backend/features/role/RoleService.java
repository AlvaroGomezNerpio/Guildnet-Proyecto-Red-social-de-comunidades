package com.guildnet.backend.features.role;

import com.guildnet.backend.common.exception.BadRequestException;
import com.guildnet.backend.common.exception.ConflictException;
import com.guildnet.backend.common.exception.ForbiddenException;
import com.guildnet.backend.common.exception.NotFoundException;
import com.guildnet.backend.common.mapper.DtoMapper;
import com.guildnet.backend.features.community.Community;
import com.guildnet.backend.features.community.CommunityColors;
import com.guildnet.backend.features.community.CommunityRepository;
import com.guildnet.backend.features.profile.CommunityProfileRepository;
import com.guildnet.backend.features.permission.Permission;
import com.guildnet.backend.features.permission.PermissionRepository;
import com.guildnet.backend.features.permission.PermissionType;
import com.guildnet.backend.features.role.dto.RoleDTO;
import com.guildnet.backend.features.role.dto.RolePermissionDTO;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Gestión de roles y de sus permisos. Requiere ASSIGN_ROLES en la comunidad del rol.
 * Para evitar escalada de privilegios, salvo el creador de la comunidad, nadie puede
 * modificar el rol que tiene asignado.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final CommunityRepository communityRepository;
    private final CommunityProfileRepository profileRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final AccessGuard accessGuard;

    // ------------------------------------------------------------------ Roles

    public RoleDTO create(RoleDTO dto, User user) {
        if (dto.communityId() == null) {
            throw new BadRequestException("La comunidad es obligatoria");
        }
        Community community = communityRepository.findById(dto.communityId())
                .orElseThrow(() -> new NotFoundException("Comunidad no encontrada"));
        accessGuard.requirePermission(user, community.getId(), PermissionType.ASSIGN_ROLES);
        String name = dto.name().trim();
        if (roleRepository.existsByCommunityIdAndNameIgnoreCase(community.getId(), name)) {
            throw new ConflictException("Ya existe un rol con ese nombre en la comunidad");
        }

        // Sin colores: el color de la comunidad, con el texto que mejor se lea encima
        String background = CommunityColors.normalize(dto.backgroundColor(), community.getPrimaryColor());
        Role role = roleRepository.save(Role.builder()
                .name(name)
                .textColor(CommunityColors.normalize(dto.textColor(), CommunityColors.readableOn(background)))
                .backgroundColor(background)
                .community(community)
                .build());
        return DtoMapper.toRoleDTO(role);
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> getByCommunity(Long communityId) {
        return roleRepository.findByCommunityIdOrderByIdAsc(communityId).stream().map(DtoMapper::toRoleDTO).toList();
    }

    @Transactional(readOnly = true)
    public RoleDTO getById(Long id) {
        return DtoMapper.toRoleDTO(load(id));
    }

    public RoleDTO update(Long id, RoleDTO dto, User user) {
        Role role = loadManageable(id, user);
        String name = dto.name().trim();
        if (roleRepository.existsByCommunityIdAndNameIgnoreCaseAndIdNot(role.getCommunity().getId(), name, id)) {
            throw new ConflictException("Ya existe un rol con ese nombre en la comunidad");
        }
        role.setName(name);
        role.setTextColor(CommunityColors.normalize(dto.textColor(), role.getTextColor()));
        role.setBackgroundColor(CommunityColors.normalize(dto.backgroundColor(), role.getBackgroundColor()));
        return DtoMapper.toRoleDTO(role);
    }

    /** Borra el rol y lo quita de los perfiles que lo tenían. */
    public void delete(Long id, User user) {
        Role role = loadManageable(id, user);
        profileRepository.findByRoleId(id).forEach(profile -> profile.setRole(null));
        roleRepository.delete(role);
    }

    // ------------------------------------------------------------------ Permisos de un rol

    @Transactional(readOnly = true)
    public List<RolePermissionDTO> getPermissions(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId).stream().map(RoleService::toDTO).toList();
    }

    public RolePermissionDTO addPermission(Long roleId, Long permissionId, User user) {
        Role role = loadManageable(roleId, user);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new NotFoundException("Permiso no encontrado"));
        if (rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            throw new ConflictException("El rol ya tiene ese permiso");
        }
        RolePermission rolePermission = RolePermission.builder().role(role).permission(permission).build();
        role.getPermissions().add(rolePermission);
        return toDTO(rolePermissionRepository.save(rolePermission));
    }

    public void removePermission(Long rolePermissionId, User user) {
        RolePermission rolePermission = rolePermissionRepository.findById(rolePermissionId)
                .orElseThrow(() -> new NotFoundException("Permiso de rol no encontrado"));
        Role role = loadManageable(rolePermission.getRole().getId(), user);
        role.getPermissions().remove(rolePermission);
        rolePermissionRepository.delete(rolePermission);
    }

    // ------------------------------------------------------------------ Utilidades

    private Role load(Long id) {
        return roleRepository.findById(id).orElseThrow(() -> new NotFoundException("Rol no encontrado"));
    }

    /** Carga el rol comprobando ASSIGN_ROLES y que no sea el rol propio (salvo para el creador). */
    private Role loadManageable(Long id, User user) {
        Role role = load(id);
        Community community = role.getCommunity();
        accessGuard.requirePermission(user, community.getId(), PermissionType.ASSIGN_ROLES);

        if (!accessGuard.isCommunityOwner(user, community)) {
            boolean isOwnRole = accessGuard.findMembership(user, community.getId())
                    .map(profile -> profile.getRole() != null && Objects.equals(profile.getRole().getId(), id))
                    .orElse(false);
            if (isOwnRole) {
                throw new ForbiddenException("No puedes modificar tu propio rol");
            }
        }
        return role;
    }

    private static RolePermissionDTO toDTO(RolePermission rp) {
        return new RolePermissionDTO(
                rp.getId(),
                rp.getRole().getId(),
                rp.getRole().getName(),
                rp.getPermission().getId(),
                rp.getPermission().getName().name()
        );
    }
}
