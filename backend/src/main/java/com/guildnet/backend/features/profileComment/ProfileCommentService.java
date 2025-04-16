package com.guildnet.backend.features.profileComment;

import com.guildnet.backend.features.profileComment.dto.ProfileCommentCreateUpdateDTO;
import com.guildnet.backend.features.profileComment.dto.ProfileCommentDTO;

import java.util.List;

public interface ProfileCommentService {

    ProfileCommentDTO createProfileComment(Long authorProfileId, Long targetProfileId, ProfileCommentCreateUpdateDTO dto);

    ProfileCommentDTO updateProfileComment(Long commentId, ProfileCommentCreateUpdateDTO dto);

    List<ProfileCommentDTO> getCommentsByTargetProfileId(Long targetProfileId);

    void deleteProfileComment(Long commentId);
}

