package com.guildnet.backend.features.title;

import com.guildnet.backend.common.exception.ConflictException;
import com.guildnet.backend.common.exception.NotFoundException;
import com.guildnet.backend.common.mapper.DtoMapper;
import com.guildnet.backend.features.community.Community;
import com.guildnet.backend.features.community.CommunityColors;
import com.guildnet.backend.features.community.CommunityRepository;
import com.guildnet.backend.features.profile.CommunityProfile;
import com.guildnet.backend.features.profile.CommunityProfileRepository;
import com.guildnet.backend.features.notification.NotificationService;
import com.guildnet.backend.features.notification.NotificationType;
import com.guildnet.backend.features.permission.PermissionType;
import com.guildnet.backend.features.title.dto.CreateTitleRequest;
import com.guildnet.backend.features.title.dto.TitleDTO;
import com.guildnet.backend.features.title.dto.UpdateTitleRequest;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Títulos de una comunidad. Crear, editar y borrar requiere ASSIGN_TITLES. */
@Service
@RequiredArgsConstructor
@Transactional
public class TitleService {

    private final TitleRepository titleRepository;
    private final CommunityRepository communityRepository;
    private final CommunityProfileRepository profileRepository;
    private final NotificationService notificationService;
    private final AccessGuard accessGuard;

    public TitleDTO create(CreateTitleRequest request, User user) {
        Community community = communityRepository.findById(request.communityId())
                .orElseThrow(() -> new NotFoundException("Comunidad no encontrada"));
        accessGuard.requirePermission(user, community.getId(), PermissionType.ASSIGN_TITLES);
        String text = request.title().trim();
        if (titleRepository.existsByCommunityIdAndTitleIgnoreCase(community.getId(), text)) {
            throw new ConflictException("Ya existe un título con ese nombre en la comunidad");
        }

        // Sin colores: el color de la comunidad, con el texto que mejor se lea encima
        String background = CommunityColors.normalize(request.backgroundColor(), community.getPrimaryColor());
        Title title = titleRepository.save(Title.builder()
                .title(text)
                .textColor(CommunityColors.normalize(request.textColor(), CommunityColors.readableOn(background)))
                .backgroundColor(background)
                .community(community)
                .build());
        return DtoMapper.toTitleDTO(title);
    }

    public TitleDTO update(Long id, UpdateTitleRequest request, User user) {
        Title title = loadManageable(id, user);
        if (request.title() != null && !request.title().isBlank()) {
            String text = request.title().trim();
            if (titleRepository.existsByCommunityIdAndTitleIgnoreCaseAndIdNot(title.getCommunity().getId(), text, id)) {
                throw new ConflictException("Ya existe un título con ese nombre en la comunidad");
            }
            title.setTitle(text);
        }
        title.setTextColor(CommunityColors.normalize(request.textColor(), title.getTextColor()));
        title.setBackgroundColor(CommunityColors.normalize(request.backgroundColor(), title.getBackgroundColor()));
        return DtoMapper.toTitleDTO(title);
    }

    /** Borra el título, quitándolo antes de los perfiles que lo tienen (incluido como destacado). */
    public void delete(Long id, User user) {
        Title title = loadManageable(id, user);
        CommunityProfile actor = accessGuard.findMembership(user, title.getCommunity().getId()).orElse(null);
        profileRepository.findByFeaturedTitleId(id).forEach(profile -> profile.setFeaturedTitle(null));
        title.getProfiles().forEach(profile -> {
            profile.getTitles().remove(title);
            notificationService.notify(profile, actor, NotificationType.TITLE_REMOVED,
                    "Te ha eliminado el título \"" + title.getTitle() + "\".");
        });
        title.getProfiles().clear();
        titleRepository.delete(title);
    }

    @Transactional(readOnly = true)
    public TitleDTO getById(Long id) {
        return DtoMapper.toTitleDTO(load(id));
    }

    @Transactional(readOnly = true)
    public List<TitleDTO> getByCommunity(Long communityId) {
        return titleRepository.findByCommunityId(communityId).stream().map(DtoMapper::toTitleDTO).toList();
    }

    private Title load(Long id) {
        return titleRepository.findById(id).orElseThrow(() -> new NotFoundException("Título no encontrado"));
    }

    private Title loadManageable(Long id, User user) {
        Title title = load(id);
        accessGuard.requirePermission(user, title.getCommunity().getId(), PermissionType.ASSIGN_TITLES);
        return title;
    }
}
