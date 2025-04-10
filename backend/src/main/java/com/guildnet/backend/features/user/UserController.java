package com.guildnet.backend.features.user;

import com.guildnet.backend.features.user.dto.UpdateUserRequest;
import com.guildnet.backend.features.user.dto.UserDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        UserDTO dto = new UserDTO(user.getId(), user.getTrueUsername(), user.getEmail(), user.getProfileImage());
        return ResponseEntity.ok(dto);
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> updateUser(
            @RequestPart("user") UpdateUserRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        UserDTO updated = userService.updateUserProfile(request, imageFile, user);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
