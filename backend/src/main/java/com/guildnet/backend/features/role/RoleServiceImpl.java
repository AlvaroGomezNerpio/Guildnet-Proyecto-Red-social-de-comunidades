package com.guildnet.backend.features.role;

import com.guildnet.backend.features.Community.Community;
import com.guildnet.backend.features.Community.CommunityRepository;
import com.guildnet.backend.features.role.dto.RoleDTO;
import com.guildnet.backend.features.rolePermission.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final CommunityRepository communityRepository;
    private final RolePermissionRepository rolePermissionRepository;


    @Override
    public RoleDTO createRole(RoleDTO roleDTO) {
        Community community = communityRepository.findById(roleDTO.getCommunityId())
                .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));

        Role role = Role.builder()
                .name(roleDTO.getName())
                .textColor(roleDTO.getTextColor())
                .backgroundColor(roleDTO.getBackgroundColor())
                .community(community)
                .build();

        Role saved = roleRepository.save(role);
        return mapToDTO(saved);
    }

    @Override
    public List<RoleDTO> getRolesByCommunity(Long communityId) {
        Community community = Community.builder().id(communityId).build();
        List<Role> roles = roleRepository.findByCommunity(community);
        return roles.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    @Override
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        return mapToDTO(role);
    }

    @Override
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        role.setName(roleDTO.getName());
        role.setTextColor(roleDTO.getTextColor());
        role.setBackgroundColor(roleDTO.getBackgroundColor());

        Role updated = roleRepository.save(role);
        return mapToDTO(updated);
    }

    @Override
    public void deleteRole(Long id) {
        // 🧹 Primero borramos los RolePermission relacionados
        rolePermissionRepository.deleteByRoleId(id);

        // 🧹 Después borramos el Role
        roleRepository.deleteById(id);
    }

    private RoleDTO mapToDTO(Role role) {
        return new RoleDTO(
                role.getId(),
                role.getName(),
                role.getTextColor(),
                role.getBackgroundColor(),
                role.getCommunity().getId()
        );
    }
}

