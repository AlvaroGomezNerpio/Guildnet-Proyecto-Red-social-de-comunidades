package com.guildnet.backend.features.title;

import com.guildnet.backend.features.Community.Community;
import com.guildnet.backend.features.Community.CommunityRepository;
import com.guildnet.backend.features.title.dto.CreateTitleRequest;
import com.guildnet.backend.features.title.dto.TitleDTO;
import com.guildnet.backend.features.title.dto.UpdateTitleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TitleServiceImpl implements TitleService {

    private final TitleRepository titleRepository;
    private final CommunityRepository communityRepository;

    @Override
    public TitleDTO createTitle(CreateTitleRequest request) {
        Community community = communityRepository.findById(request.getCommunityId())
                .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));

        Title title = Title.builder()
                .title(request.getTitle())
                .textColor(request.getTextColor())
                .backgroundColor(request.getBackgroundColor())
                .community(community)
                .build();

        Title saved = titleRepository.save(title);
        return mapToDTO(saved);
    }

    @Override
    public TitleDTO updateTitle(Long id, UpdateTitleRequest request) {
        Title title = titleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Título no encontrado"));

        if (request.getTitle() != null) title.setTitle(request.getTitle());
        if (request.getTextColor() != null) title.setTextColor(request.getTextColor());
        if (request.getBackgroundColor() != null) title.setBackgroundColor(request.getBackgroundColor());

        Title updated = titleRepository.save(title);
        return mapToDTO(updated);
    }

    @Override
    public void deleteTitle(Long id) {
        Title title = titleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Título no encontrado"));

        // 🔸 Eliminar relaciones con perfiles (evita fallo de integridad referencial)
        if (title.getProfiles() != null) {
            title.getProfiles().forEach(profile -> profile.getTitles().remove(title));
            title.getProfiles().clear();
        }

        titleRepository.save(title); // Guardamos la entidad sin relaciones

        titleRepository.delete(title); // Ahora sí, se puede eliminar
    }


    @Override
    public List<TitleDTO> getTitlesByCommunity(Long communityId) {
        return titleRepository.findByCommunityId(communityId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public TitleDTO getTitleById(Long id) {
        Title title = titleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Título no encontrado"));
        return mapToDTO(title);
    }

    private TitleDTO mapToDTO(Title title) {
        return new TitleDTO(
                title.getId(),
                title.getTitle(),
                title.getTextColor(),
                title.getBackgroundColor(),
                title.getCommunity().getId()
        );
    }


}
