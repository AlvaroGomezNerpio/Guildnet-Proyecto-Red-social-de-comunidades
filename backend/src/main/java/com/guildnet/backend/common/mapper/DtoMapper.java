package com.guildnet.backend.common.mapper;

import com.guildnet.backend.features.community.Community;
import com.guildnet.backend.features.community.dto.CommunityResponseDTO;
import com.guildnet.backend.features.profile.CommunityProfile;
import com.guildnet.backend.features.profile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.post.Post;
import com.guildnet.backend.features.post.dto.PostDTO;
import com.guildnet.backend.features.postcomment.PostComment;
import com.guildnet.backend.features.postcomment.dto.PostCommentDTO;
import com.guildnet.backend.features.role.Role;
import com.guildnet.backend.features.role.dto.RoleDTO;
import com.guildnet.backend.features.title.Title;
import com.guildnet.backend.features.title.dto.TitleDTO;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.features.user.dto.UserDTO;

import java.util.List;

/** Conversión centralizada de entidades JPA a DTOs de la API. */
public final class DtoMapper {

    private DtoMapper() {
    }

    public static UserDTO toUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getProfileImage(),
                user.getTags() != null ? List.copyOf(user.getTags()) : List.of()
        );
    }

    public static CommunityResponseDTO toCommunityDTO(Community community) {
        return new CommunityResponseDTO(
                community.getId(),
                community.getName(),
                community.getDescription(),
                community.getRules(),
                community.getImage(),
                community.getBanner(),
                community.getTags() != null ? List.copyOf(community.getTags()) : List.of(),
                community.getOwner() != null ? community.getOwner().getId() : null
        );
    }

    public static RoleDTO toRoleDTO(Role role) {
        if (role == null) {
            return null;
        }
        return new RoleDTO(
                role.getId(),
                role.getName(),
                role.getTextColor(),
                role.getBackgroundColor(),
                role.getCommunity().getId()
        );
    }

    public static TitleDTO toTitleDTO(Title title) {
        if (title == null) {
            return null;
        }
        return new TitleDTO(
                title.getId(),
                title.getTitle(),
                title.getTextColor(),
                title.getBackgroundColor(),
                title.getCommunity().getId()
        );
    }

    public static CommunityProfileDTO toProfileDTO(CommunityProfile profile) {
        if (profile == null) {
            return null;
        }
        List<TitleDTO> titles = profile.getTitles() != null
                ? profile.getTitles().stream().map(DtoMapper::toTitleDTO).toList()
                : List.of();

        return new CommunityProfileDTO(
                profile.getId(),
                profile.getUsername(),
                profile.getDescription(),
                profile.getProfileImage(),
                toTitleDTO(profile.getFeaturedTitle()),
                profile.getUser().getId(),
                profile.getCommunity().getId(),
                toRoleDTO(profile.getRole()),
                titles
        );
    }

    public static PostDTO toPostDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setTags(post.getTags() != null ? List.copyOf(post.getTags()) : List.of());
        dto.setLikes(post.getLikeCount());
        dto.setComments(post.getCommentCount());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setCommunityId(post.getCommunity().getId());
        dto.setCommunityProfile(toProfileDTO(post.getProfile()));
        return dto;
    }

    public static PostCommentDTO toPostCommentDTO(PostComment comment) {
        return new PostCommentDTO(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                toProfileDTO(comment.getProfile())
        );
    }
}
