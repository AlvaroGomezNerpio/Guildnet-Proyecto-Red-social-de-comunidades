package com.guildnet.backend.features.communityProfile;

import com.guildnet.backend.features.Community.Community;
import com.guildnet.backend.features.Community.CommunityRepository;
import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.communityProfile.dto.CreateCommunityProfileRequest;
import com.guildnet.backend.features.communityProfile.dto.UpdateCommunityProfileRequest;
import com.guildnet.backend.features.role.Role;
import com.guildnet.backend.features.role.RoleRepository;
import com.guildnet.backend.features.role.dto.RoleDTO;
import com.guildnet.backend.features.title.Title;
import com.guildnet.backend.features.title.TitleRepository;
import com.guildnet.backend.features.title.dto.TitleDTO;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.features.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityProfileServiceImpl implements CommunityProfileService {

    private final CommunityProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final TitleRepository titleRepository;
    private final RoleRepository roleRepository;

    @Override
    public CommunityProfileDTO createProfile(CreateCommunityProfileRequest request, Long userId, Long communityId) {
        User user = getUserById(userId);
        Community community = getCommunityById(communityId);

        CommunityProfile profile = CommunityProfile.builder()
                .username(request.getUsername())
                .description(request.getDescription())
                .user(user)
                .community(community)
                .titles(new ArrayList<>())
                .build();

        CommunityProfile saved = profileRepository.save(profile);
        return mapToProfileDTO(saved);
    }

    @Override
    public CommunityProfileDTO createProfileAutomatically(User user, Long communityId) {
        Community community = getCommunityById(communityId);

        String copiedImageUrl = null;

        if (user.getProfileImage() != null) {
            try {
                String relativePath = user.getProfileImage().replace("http://localhost:8080/", "");
                Path originalPath = Paths.get(relativePath);

                if (!Files.exists(originalPath)) {
                    throw new RuntimeException("La imagen de perfil del usuario no existe: " + originalPath);
                }

                copiedImageUrl = copyProfileImage(originalPath);
            } catch (Exception e) {
                throw new RuntimeException("Error al copiar la imagen de perfil", e);
            }
        }

        CommunityProfile profile = CommunityProfile.builder()
                .username(user.getTrueUsername())
                .profileImage(copiedImageUrl)
                .description(null)
                .user(user)
                .community(community)
                .titles(new ArrayList<>())
                .build();

        CommunityProfile saved = profileRepository.save(profile);
        return mapToProfileDTO(saved);
    }

    @Override
    public CommunityProfileDTO updateProfile(Long profileId, UpdateCommunityProfileRequest request, MultipartFile imageFile) {
        CommunityProfile profile = getProfileById(profileId);

        if (request.getDescription() != null) {
            profile.setDescription(request.getDescription());
        }

        if (request.getUserName() != null) {
            profile.setUsername(request.getUserName());
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = saveProfileImage(imageFile);
            profile.setProfileImage(imageUrl);
        }

        CommunityProfile updated = profileRepository.save(profile);
        return mapToProfileDTO(updated);
    }

    @Override
    public CommunityProfileDTO getProfileDtoById(Long profileId) {
        CommunityProfile profile = getProfileById(profileId);
        return mapToProfileDTO(profile);
    }

    @Override
    public List<CommunityProfileDTO> getProfilesByCommunity(Long communityId) {
        return profileRepository.findByCommunityId(communityId)
                .stream()
                .map(this::mapToProfileDTO)
                .toList();
    }

    @Override
    public Optional<CommunityProfileDTO> getProfile(Long userId, Long communityId) {
        return profileRepository.findByUserIdAndCommunityId(userId, communityId)
                .map(this::mapToProfileDTO);
    }

    @Override
    public void deleteProfile(Long profileId) {
        CommunityProfile profile = getProfileById(profileId);
        profileRepository.delete(profile);
    }

    @Override
    public void assignRole(Long profileId, Long roleId) {
        CommunityProfile profile = getProfileById(profileId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        profile.setRole(role);
        profileRepository.save(profile);
    }

    @Override
    public void removeRole(Long profileId, Long roleId) {
        CommunityProfile profile = getProfileById(profileId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        if (profile.getRole() != null && profile.getRole().getId().equals(role.getId())) {
            profile.setRole(null);
            profileRepository.save(profile);
        }
    }

    @Override
    public List<RoleDTO> getRolesByProfile(Long profileId) {
        CommunityProfile profile = getProfileById(profileId);

        if (profile.getRole() == null) return List.of();

        return List.of(mapToRoleDTO(profile.getRole()));
    }

    private RoleDTO mapToRoleDTO(Role role) {
        return new RoleDTO(
                role.getId(),
                role.getName(),
                role.getTextColor(),
                role.getBackgroundColor(),
                role.getCommunity().getId()
        );
    }

    private CommunityProfileDTO mapToProfileDTO(CommunityProfile profile) {
        Title featuredTitle = profile.getFeaturedTitle();

        RoleDTO roleDTO = profile.getRole() != null
                ? mapToRoleDTO(profile.getRole())
                : null;

        List<TitleDTO> titleDTOs = profile.getTitles() != null
                ? profile.getTitles().stream().map(this::mapToTitleDTO).toList()
                : List.of();

        return new CommunityProfileDTO(
                profile.getId(),
                profile.getUsername(),
                profile.getDescription(),
                profile.getProfileImage(),
                featuredTitle != null ? mapToTitleDTO(featuredTitle) : null,
                profile.getUser().getId(),
                profile.getCommunity().getId(),
                roleDTO,             // ✅ un único rol
                titleDTOs
        );
    }


    private TitleDTO mapToTitleDTO(Title title) {
        return new TitleDTO(
                title.getId(),
                title.getTitle(),
                title.getTextColor(),
                title.getBackgroundColor(),
                title.getCommunity().getId()
        );
    }

    private String saveProfileImage(MultipartFile file) {
        try {
            Path uploadPath = Paths.get("uploads/community-profiles/");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(uniqueName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "http://localhost:8080/uploads/community-profiles/" + uniqueName;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen", e);
        }
    }

    private String copyProfileImage(Path sourcePath) {
        try {
            Path targetFolder = Paths.get("uploads/community-profiles/");
            if (!Files.exists(targetFolder)) {
                Files.createDirectories(targetFolder);
            }

            String uniqueFileName = UUID.randomUUID() + "_" + sourcePath.getFileName();
            Path targetPath = targetFolder.resolve(uniqueFileName);
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            return "http://localhost:8080/uploads/community-profiles/" + uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Error al copiar la imagen", e);
        }
    }

    @Override
    public void assignTitle(Long profileId, Long titleId) {
        CommunityProfile profile = getProfileById(profileId);
        Title title = getTitleById(titleId);

        if (!profile.getTitles().contains(title)) {
            profile.getTitles().add(title);
            profileRepository.save(profile);
        }
    }

    @Override
    public void removeTitle(Long profileId, Long titleId) {
        CommunityProfile profile = getProfileById(profileId);
        Title title = getTitleById(titleId);

        if (profile.getTitles().contains(title)) {
            profile.getTitles().remove(title);
            profileRepository.save(profile);
        }
    }

    @Override
    public void changeFeaturedTitle(Long profileId, Long titleId) {
        CommunityProfile profile = getProfileById(profileId);
        Title title = getTitleById(titleId);

        if (!profile.getTitles().contains(title)) {
            throw new RuntimeException("El título no está asignado al perfil");
        }

        profile.setFeaturedTitle(title);
        profileRepository.save(profile);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private Community getCommunityById(Long communityId) {
        return communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));
    }

    private CommunityProfile getProfileById(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));
    }

    private Title getTitleById(Long titleId) {
        return titleRepository.findById(titleId)
                .orElseThrow(() -> new RuntimeException("Título no encontrado"));
    }
}




