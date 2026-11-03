package com.guildnet.backend.features.role;

import com.guildnet.backend.features.role.dto.RoleDTO;
import com.guildnet.backend.features.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleDTO create(@RequestBody @Valid RoleDTO dto, @AuthenticationPrincipal User user) {
        return roleService.create(dto, user);
    }

    @GetMapping("/community/{communityId}")
    public List<RoleDTO> getByCommunity(@PathVariable Long communityId) {
        return roleService.getByCommunity(communityId);
    }

    @GetMapping("/{id}")
    public RoleDTO getById(@PathVariable Long id) {
        return roleService.getById(id);
    }

    @PutMapping("/{id}")
    public RoleDTO update(@PathVariable Long id, @RequestBody @Valid RoleDTO dto, @AuthenticationPrincipal User user) {
        return roleService.update(id, dto, user);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        roleService.delete(id, user);
        return "Rol eliminado correctamente";
    }
}
