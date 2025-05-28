package com.guildnet.backend.features.communityProfile;

import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.communityProfile.dto.CreateCommunityProfileRequest;
import com.guildnet.backend.features.communityProfile.dto.UpdateCommunityProfileRequest;
import com.guildnet.backend.features.role.dto.RoleDTO;
import com.guildnet.backend.features.user.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface CommunityProfileService {
    CommunityProfileDTO createProfile(CreateCommunityProfileRequest request, Long userId, Long communityId);
    List<CommunityProfileDTO> getProfilesByCommunity(Long communityId);
    CommunityProfileDTO getProfileDtoById(Long profileId);
    Optional<CommunityProfileDTO> getProfile(Long userId, Long communityId);
    CommunityProfileDTO createProfileAutomatically(User user, Long communityId);
    CommunityProfileDTO updateProfile(Long profileId, UpdateCommunityProfileRequest request, MultipartFile imageFile);
    void deleteProfile(Long profileId);

    void assignTitle(Long profileId, Long titleId);
    void removeTitle(Long profileId, Long titleId);
    void changeFeaturedTitle(Long profileId, Long titleId);

    void assignRole(Long profileId, Long roleId);
    void removeRole(Long profileId, Long roleId);
    List<RoleDTO> getRolesByProfile(Long profileId);


}

