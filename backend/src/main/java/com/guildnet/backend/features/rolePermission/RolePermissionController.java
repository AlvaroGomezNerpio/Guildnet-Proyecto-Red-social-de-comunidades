package com.guildnet.backend.features.rolePermission;

import com.guildnet.backend.features.role.RoleService;
import com.guildnet.backend.features.rolePermission.dto.CreateRolePermissionRequest;
import com.guildnet.backend.features.rolePermission.dto.RolePermissionDTO;
import com.guildnet.backend.features.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/role-permissions")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RoleService roleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RolePermissionDTO add(@RequestBody @Valid CreateRolePermissionRequest request, @AuthenticationPrincipal User user) {
        return roleService.addPermission(request.roleId(), request.permissionId(), user);
    }

    @GetMapping("/role/{roleId}")
    public List<RolePermissionDTO> getByRole(@PathVariable Long roleId) {
        return roleService.getPermissions(roleId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long id, @AuthenticationPrincipal User user) {
        roleService.removePermission(id, user);
    }
}
