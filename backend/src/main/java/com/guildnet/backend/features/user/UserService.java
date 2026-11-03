package com.guildnet.backend.features.user;

import com.guildnet.backend.common.exception.ConflictException;
import com.guildnet.backend.common.exception.NotFoundException;
import com.guildnet.backend.common.mapper.DtoMapper;
import com.guildnet.backend.features.community.CommunityRepository;
import com.guildnet.backend.features.user.dto.UpdateUserRequest;
import com.guildnet.backend.features.user.dto.UserDTO;
import com.guildnet.backend.storage.FileStorageService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final FileStorageService fileStorageService;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public UserDTO getUser(Long userId) {
        return DtoMapper.toUserDTO(load(userId));
    }

    public UserDTO updateProfile(Long userId, UpdateUserRequest request, MultipartFile image) {
        User user = load(userId);

        if (request.username() != null && !request.username().isBlank()) {
            user.setDisplayName(request.username().trim());
        }
        if (request.email() != null && !request.email().isBlank()) {
            String email = request.email().trim().toLowerCase(Locale.ROOT);
            if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new ConflictException("El correo ya está en uso");
            }
            user.setEmail(email);
        }
        if (request.tags() != null) {
            replaceTags(user, request.tags());
        }
        String imageUrl = fileStorageService.storeImage(image, FileStorageService.Folder.USERS);
        if (imageUrl != null) {
            user.setProfileImage(imageUrl);
        }
        return DtoMapper.toUserDTO(user);
    }

    public void updateTags(Long userId, List<String> tags) {
        replaceTags(load(userId), tags);
    }

    /** Elimina la cuenta del usuario, incluidas las comunidades que ha creado. */
    public void deleteAccount(Long userId) {
        communityRepository.deleteAll(communityRepository.findByOwnerId(userId));
        // Se limpia el contexto para que el usuario se recargue sin los perfiles ya borrados
        entityManager.flush();
        entityManager.clear();
        userRepository.delete(load(userId));
    }

    private void replaceTags(User user, List<String> tags) {
        List<String> cleaned = new ArrayList<>(tags.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(String::trim)
                .distinct()
                .toList());
        user.getTags().clear();
        user.getTags().addAll(cleaned);
    }

    private User load(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }
}
