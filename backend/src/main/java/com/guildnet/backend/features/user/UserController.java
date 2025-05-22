package com.guildnet.backend.features.user;

import com.guildnet.backend.features.user.dto.UpdateUserRequest;
import com.guildnet.backend.features.user.dto.UpdateUserTagsRequest;
import com.guildnet.backend.features.user.dto.UserDTO;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    @Transactional
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();

        // Reobtener el usuario desde base de datos para que esté en sesión activa
        User user = userRepository.findByEmail(authUser.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Forzar la carga de los tags
        user.getTags().size();

        UserDTO dto = new UserDTO(
                user.getId(),
                user.getTrueUsername(),
                user.getEmail(),
                user.getProfileImage(),
                user.getTags()
        );
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

    @PutMapping("/tags")
    public ResponseEntity<?> updateTags(
            @RequestBody UpdateUserTagsRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        userService.updateUserTags(user, request.getTags());
        return ResponseEntity.ok("Tags actualizados");
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
