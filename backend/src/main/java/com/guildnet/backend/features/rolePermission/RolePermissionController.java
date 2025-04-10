package com.guildnet.backend.features.rolePermission;

import com.guildnet.backend.features.rolePermission.dto.CreateRolePermissionRequest;
import com.guildnet.backend.features.rolePermission.dto.RolePermissionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/role-permissions")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @PostMapping
    public ResponseEntity<RolePermissionDTO> assignPermissionToRole(@RequestBody CreateRolePermissionRequest request) {
        RolePermissionDTO assigned = rolePermissionService.assignPermissionToRole(
                request.getRoleId(), request.getPermissionId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(assigned);
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<RolePermissionDTO>> getPermissionsByRole(@PathVariable Long roleId) {
        List<RolePermissionDTO> rolePermissions = rolePermissionService.getPermissionsByRole(roleId);
        return ResponseEntity.ok(rolePermissions);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeRolePermission(@PathVariable Long id) {
        rolePermissionService.removeRolePermission(id);
        return ResponseEntity.noContent().build();
    }
}



