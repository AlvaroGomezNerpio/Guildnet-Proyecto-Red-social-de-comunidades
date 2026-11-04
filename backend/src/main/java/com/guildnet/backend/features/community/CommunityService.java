package com.guildnet.backend.features.community;

import com.guildnet.backend.common.exception.ConflictException;
import com.guildnet.backend.common.exception.ForbiddenException;
import com.guildnet.backend.common.exception.NotFoundException;
import com.guildnet.backend.common.html.HtmlSanitizer;
import com.guildnet.backend.common.mapper.DtoMapper;
import com.guildnet.backend.features.community.dto.CommunityResponseDTO;
import com.guildnet.backend.features.community.dto.CreateCommunityRequest;
import com.guildnet.backend.features.community.dto.UpdateCommunityRequest;
import com.guildnet.backend.features.profile.CommunityProfile;
import com.guildnet.backend.features.profile.CommunityProfileRepository;
import com.guildnet.backend.features.profile.CommunityProfileService;
import com.guildnet.backend.features.permission.PermissionRepository;
import com.guildnet.backend.features.permission.PermissionType;
import com.guildnet.backend.features.role.Role;
import com.guildnet.backend.features.role.RoleRepository;
import com.guildnet.backend.features.role.RolePermission;
import com.guildnet.backend.features.role.RolePermissionRepository;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.features.user.UserRepository;
import com.guildnet.backend.security.AccessGuard;
import com.guildnet.backend.storage.FileStorageService;
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
public class CommunityService {

    static final String LEADER_ROLE_NAME = "Líder";

    private final CommunityRepository communityRepository;
    private final CommunityProfileRepository profileRepository;
    private final CommunityProfileService profileService;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AccessGuard accessGuard;

    // ------------------------------------------------------------------ Consultas

    @Transactional(readOnly = true)
    public List<CommunityResponseDTO> getAll() {
        return toDTOs(communityRepository.findAll());
    }

    @Transactional(readOnly = true)
    public CommunityResponseDTO getById(Long id) {
        return DtoMapper.toCommunityDTO(load(id));
    }

    @Transactional(readOnly = true)
    public List<CommunityResponseDTO> search(String name, List<String> tags) {
        return toDTOs(communityRepository.findAll(CommunitySpecifications.search(name, tags)));
    }

    @Transactional(readOnly = true)
    public List<CommunityResponseDTO> getSubscribed(User user) {
        return toDTOs(communityRepository.findCommunitiesByUserId(user.getId()));
    }

    @Transactional(readOnly = true)
    public List<CommunityResponseDTO> getSuggested(User authUser) {
        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        if (user.getTags().isEmpty()) {
            return List.of();
        }
        List<String> tags = user.getTags().stream().map(t -> t.toLowerCase(Locale.ROOT)).toList();
        return toDTOs(communityRepository.findSuggestedCommunities(tags, user.getId()));
    }

    @Transactional(readOnly = true)
    public List<CommunityResponseDTO> getMostPopular() {
        return toDTOs(communityRepository.findCommunitiesOrderBySubscribersDesc());
    }

    // ------------------------------------------------------------------ Escritura

    /**
     * Crea la comunidad, el perfil del creador dentro de ella y el rol "Líder"
     * con todos los permisos, asignado al creador. Todo en una única transacción.
     */
    public CommunityResponseDTO create(CreateCommunityRequest request, MultipartFile image, MultipartFile banner, User authUser) {
        User owner = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        String name = request.name().trim();
        if (communityRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe una comunidad con ese nombre");
        }

        Community community = Community.builder()
                .name(name)
                .description(HtmlSanitizer.sanitize(request.description()))
                .rules(HtmlSanitizer.sanitize(request.rules()))
                .tags(cleanTags(request.tags()))
                .primaryColor(CommunityColors.normalize(request.primaryColor(), CommunityColors.DEFAULT_PRIMARY))
                .accentColor(CommunityColors.normalize(request.accentColor(), CommunityColors.DEFAULT_ACCENT))
                .owner(owner)
                .image(fileStorageService.storeImage(image, FileStorageService.Folder.COMMUNITIES))
                .banner(fileStorageService.storeImage(banner, FileStorageService.Folder.COMMUNITIES))
                .build();
        community = communityRepository.save(community);

        CommunityProfile ownerProfile = profileService.createMembership(owner, community, null, null);

        Role leader = roleRepository.save(Role.builder()
                .name(LEADER_ROLE_NAME)
                .textColor(CommunityColors.readableOn(community.getPrimaryColor()))
                .backgroundColor(community.getPrimaryColor())
                .community(community)
                .build());
        permissionRepository.findAll().forEach(permission ->
                rolePermissionRepository.save(RolePermission.builder().role(leader).permission(permission).build()));

        ownerProfile.setRole(leader);
        return DtoMapper.toCommunityDTO(community);
    }

    public CommunityResponseDTO update(Long id, UpdateCommunityRequest request, MultipartFile image, MultipartFile banner, User user) {
        Community community = load(id);
        accessGuard.requirePermission(user, id, PermissionType.MANAGE_COMMUNITY_SETTINGS);

        String name = request.name().trim();
        if (communityRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Ya existe una comunidad con ese nombre");
        }
        community.setName(name);
        community.setDescription(HtmlSanitizer.sanitize(request.description()));
        community.setRules(HtmlSanitizer.sanitize(request.rules()));
        community.getTags().clear();
        community.getTags().addAll(cleanTags(request.tags()));
        community.setPrimaryColor(CommunityColors.normalize(request.primaryColor(), community.getPrimaryColor()));
        community.setAccentColor(CommunityColors.normalize(request.accentColor(), community.getAccentColor()));

        String imageUrl = fileStorageService.storeImage(image, FileStorageService.Folder.COMMUNITIES);
        if (imageUrl != null) {
            community.setImage(imageUrl);
        }
        String bannerUrl = fileStorageService.storeImage(banner, FileStorageService.Folder.COMMUNITIES);
        if (bannerUrl != null) {
            community.setBanner(bannerUrl);
        }
        return DtoMapper.toCommunityDTO(community);
    }

    /** Solo el creador puede borrar la comunidad. */
    public void delete(Long id, User user) {
        Community community = load(id);
        accessGuard.requireCommunityOwner(user, community);
        communityRepository.delete(community);
    }

    /** Expulsa a un miembro de la comunidad (requiere BAN_USER). El creador no puede ser expulsado. */
    public void removeMember(Long communityId, Long profileId, User user) {
        Community community = load(communityId);
        accessGuard.requirePermission(user, communityId, PermissionType.BAN_USER);

        CommunityProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new NotFoundException("Perfil no encontrado"));
        if (!profile.getCommunity().getId().equals(communityId)) {
            throw new NotFoundException("El perfil no pertenece a esta comunidad");
        }
        if (accessGuard.isCommunityOwner(profile.getUser(), community)) {
            throw new ForbiddenException("No se puede expulsar al creador de la comunidad");
        }
        profileRepository.delete(profile);
    }

    // ------------------------------------------------------------------ Utilidades

    private Community load(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comunidad no encontrada"));
    }

    private static List<CommunityResponseDTO> toDTOs(List<Community> communities) {
        return communities.stream().map(DtoMapper::toCommunityDTO).toList();
    }

    private static List<String> cleanTags(List<String> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(tags.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(String::trim)
                .distinct()
                .toList());
    }
}
