package com.guildnet.backend.features.title;

import com.guildnet.backend.features.title.dto.CreateTitleRequest;
import com.guildnet.backend.features.title.dto.TitleDTO;
import com.guildnet.backend.features.title.dto.UpdateTitleRequest;

import java.util.List;
import java.util.Optional;

public interface TitleService {
    TitleDTO createTitle(CreateTitleRequest request);
    TitleDTO updateTitle(Long id, UpdateTitleRequest request);
    void deleteTitle(Long id);
    List<TitleDTO> getTitlesByCommunity(Long communityId);
    TitleDTO getTitleById(Long id);
}
