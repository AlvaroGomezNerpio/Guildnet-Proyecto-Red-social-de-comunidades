package com.guildnet.backend.features.role;

import com.guildnet.backend.features.role.dto.RoleDTO;

import java.util.List;

public interface RoleService {
    RoleDTO createRole(RoleDTO roleDTO);
    List<RoleDTO> getRolesByCommunity(Long communityId);
    RoleDTO getRoleById(Long id);
    RoleDTO updateRole(Long id, RoleDTO roleDTO);
    void deleteRole(Long id);
}
