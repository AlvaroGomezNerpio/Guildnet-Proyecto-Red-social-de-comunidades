package com.guildnet.backend.features.category;

import com.guildnet.backend.common.exception.BadRequestException;
import com.guildnet.backend.common.exception.ConflictException;
import com.guildnet.backend.common.exception.NotFoundException;
import com.guildnet.backend.features.community.Community;
import com.guildnet.backend.features.community.CommunityColors;
import com.guildnet.backend.features.community.CommunityRepository;
import com.guildnet.backend.features.permission.PermissionType;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Categorías de publicaciones de una comunidad. Las gestiona quien puede editar
 * la comunidad (MANAGE_COMMUNITY_SETTINGS); verlas puede cualquiera.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PostCategoryService {

    /** Máximo de categorías por comunidad, para que el filtro siga siendo manejable. */
    static final int MAX_CATEGORIES = 12;

    private final PostCategoryRepository categoryRepository;
    private final CommunityRepository communityRepository;
    private final AccessGuard accessGuard;

    @Transactional(readOnly = true)
    public List<PostCategoryDTO> getByCommunity(Long communityId) {
        return categoryRepository.findByCommunityIdOrderByIdAsc(communityId).stream().map(PostCategoryDTO::of).toList();
    }

    public PostCategoryDTO create(Long communityId, PostCategoryDTO dto, User user) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NotFoundException("Comunidad no encontrada"));
        accessGuard.requirePermission(user, communityId, PermissionType.MANAGE_COMMUNITY_SETTINGS);

        String name = dto.name().trim();
        if (categoryRepository.existsByCommunityIdAndNameIgnoreCase(communityId, name)) {
            throw new ConflictException("Ya existe una categoría con ese nombre");
        }
        if (categoryRepository.countByCommunityId(communityId) >= MAX_CATEGORIES) {
            throw new ConflictException("Una comunidad puede tener como mucho " + MAX_CATEGORIES + " categorías");
        }

        String background = CommunityColors.normalize(dto.backgroundColor(), community.getPrimaryColor());
        PostCategory category = categoryRepository.save(PostCategory.builder()
                .name(name)
                .backgroundColor(background)
                .textColor(CommunityColors.normalize(dto.textColor(), CommunityColors.readableOn(background)))
                .community(community)
                .build());
        return PostCategoryDTO.of(category);
    }

    public PostCategoryDTO update(Long id, PostCategoryDTO dto, User user) {
        PostCategory category = loadManageable(id, user);
        String name = dto.name().trim();
        if (categoryRepository.existsByCommunityIdAndNameIgnoreCaseAndIdNot(category.getCommunity().getId(), name, id)) {
            throw new ConflictException("Ya existe una categoría con ese nombre");
        }
        category.setName(name);
        category.setBackgroundColor(CommunityColors.normalize(dto.backgroundColor(), category.getBackgroundColor()));
        category.setTextColor(CommunityColors.normalize(dto.textColor(), category.getTextColor()));
        return PostCategoryDTO.of(category);
    }

    /** Las publicaciones de la categoría se quedan sin categoría (ON DELETE SET NULL). */
    public void delete(Long id, User user) {
        categoryRepository.delete(loadManageable(id, user));
    }

    /**
     * Categoría que se puede asignar a una publicación de la comunidad indicada
     * (null si no se indica ninguna).
     */
    @Transactional(readOnly = true)
    public PostCategory resolveForPost(Long categoryId, Long communityId) {
        if (categoryId == null) {
            return null;
        }
        PostCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BadRequestException("La categoría no existe"));
        if (!category.getCommunity().getId().equals(communityId)) {
            throw new BadRequestException("La categoría no pertenece a esta comunidad");
        }
        return category;
    }

    private PostCategory loadManageable(Long id, User user) {
        PostCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
        accessGuard.requirePermission(user, category.getCommunity().getId(), PermissionType.MANAGE_COMMUNITY_SETTINGS);
        return category;
    }
}
