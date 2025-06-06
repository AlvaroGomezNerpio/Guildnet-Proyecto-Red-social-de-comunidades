package com.guildnet.backend.features.profileComment;

import com.guildnet.backend.features.communityProfile.CommunityProfile;
import com.guildnet.backend.features.communityProfile.CommunityProfileRepository;
import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.profileComment.dto.ProfileCommentCreateUpdateDTO;
import com.guildnet.backend.features.profileComment.dto.ProfileCommentDTO;
import com.guildnet.backend.features.role.Role;
import com.guildnet.backend.features.role.dto.RoleDTO;
import com.guildnet.backend.features.title.Title;
import com.guildnet.backend.features.title.dto.TitleDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileCommentServiceImpl implements ProfileCommentService {

    private final ProfileCommentRepository profileCommentRepository;
    private final CommunityProfileRepository profileRepository;

    @Override
    @Transactional
    public ProfileCommentDTO createProfileComment(Long authorProfileId, Long targetProfileId, ProfileCommentCreateUpdateDTO dto) {
        var author = profileRepository.findById(authorProfileId).orElseThrow();
        var target = profileRepository.findById(targetProfileId).orElseThrow();

        ProfileComment comment = ProfileComment.builder()
                .content(dto.getContent())
                .authorProfile(author)
                .targetProfile(target)
                .build();

        return mapToProfileCommentDTO(profileCommentRepository.save(comment));
    }

    @Override
    @Transactional
    public ProfileCommentDTO updateProfileComment(Long commentId, ProfileCommentCreateUpdateDTO dto) {
        ProfileComment comment = profileCommentRepository.findById(commentId).orElseThrow();
        comment.setContent(dto.getContent());
        return mapToProfileCommentDTO(profileCommentRepository.save(comment));
    }

    @Override
    public List<ProfileCommentDTO> getCommentsByTargetProfileId(Long targetProfileId) {
        return profileCommentRepository.findByTargetProfileId(targetProfileId).stream()
                .map(this::mapToProfileCommentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteProfileComment(Long commentId) {
        profileCommentRepository.deleteById(commentId);
    }

    private ProfileCommentDTO mapToProfileCommentDTO(ProfileComment comment) {
        return new ProfileCommentDTO(
                comment.getId(),
                comment.getContent(),
                mapToCommunityProfileDTO(comment.getAuthorProfile()),
                mapToCommunityProfileDTO(comment.getTargetProfile())
        );
    }

    private CommunityProfileDTO mapToCommunityProfileDTO(CommunityProfile profile) {
        RoleDTO roleDTO = profile.getRole() != null ? mapToRoleDTO(profile.getRole()) : null;

        List<TitleDTO> titleDTOs = profile.getTitles() != null
                ? profile.getTitles().stream().map(this::mapToTitleDTO).toList()
                : List.of();

        TitleDTO featuredTitleDTO = profile.getFeaturedTitle() != null
                ? mapToTitleDTO(profile.getFeaturedTitle())
                : null;

        return new CommunityProfileDTO(
                profile.getId(),
                profile.getUsername(),
                profile.getDescription(),
                profile.getProfileImage(),
                featuredTitleDTO,
                profile.getUser().getId(),
                profile.getCommunity().getId(),
                roleDTO,
                titleDTOs
        );
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

    private TitleDTO mapToTitleDTO(Title title) {
        return new TitleDTO(
                title.getId(),
                title.getTitle(),
                title.getTextColor(),
                title.getBackgroundColor(),
                title.getCommunity().getId()
        );
    }
}


