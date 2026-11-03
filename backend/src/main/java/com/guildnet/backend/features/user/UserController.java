package com.guildnet.backend.features.user;

import com.guildnet.backend.features.user.dto.UpdateUserRequest;
import com.guildnet.backend.features.user.dto.UpdateUserTagsRequest;
import com.guildnet.backend.features.user.dto.UserDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Operaciones sobre la cuenta del usuario autenticado. */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserDTO getCurrentUser(@AuthenticationPrincipal User user) {
        return userService.getUser(user.getId());
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDTO updateUser(
            @RequestPart("user") @Valid UpdateUserRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal User user
    ) {
        return userService.updateProfile(user.getId(), request, image);
    }

    @PutMapping("/tags")
    public String updateTags(@RequestBody @Valid UpdateUserTagsRequest request, @AuthenticationPrincipal User user) {
        userService.updateTags(user.getId(), request.tags());
        return "Tags actualizados";
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@AuthenticationPrincipal User user) {
        userService.deleteAccount(user.getId());
    }
}
