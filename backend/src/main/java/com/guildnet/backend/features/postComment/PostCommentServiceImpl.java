package com.guildnet.backend.features.postComment;

import com.guildnet.backend.features.communityProfile.CommunityProfile;
import com.guildnet.backend.features.communityProfile.CommunityProfileRepository;
import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.post.PostRepository;
import com.guildnet.backend.features.postComment.dto.PostComentDTO;
import com.guildnet.backend.features.postComment.dto.PostCommentCreateDTO;
import com.guildnet.backend.features.postComment.dto.PostCommentUpdateDTO;
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
public class PostCommentServiceImpl implements PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final CommunityProfileRepository profileRepository;

    @Override
    @Transactional
    public PostComentDTO createComment(PostCommentCreateDTO dto) {
        var post = postRepository.findById(dto.getPostId()).orElseThrow();
        var profile = profileRepository.findById(dto.getProfileId()).orElseThrow();

        PostComment comment = new PostComment();
        comment.setContent(dto.getContent());
        comment.setPost(post);
        comment.setProfile(profile);

        PostComment saved = postCommentRepository.save(comment);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public PostComentDTO updateComment(Long commentId, PostCommentUpdateDTO dto) {
        PostComment comment = postCommentRepository.findById(commentId).orElseThrow();
        comment.setContent(dto.getContent());
        return mapToDTO(postCommentRepository.save(comment));
    }

    @Override
    public List<PostComentDTO> getCommentsByPostId(Long postId) {
        return postCommentRepository.findByPostId(postId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteComment(Long commentId) {
        postCommentRepository.deleteById(commentId);
    }

    private PostComentDTO mapToDTO(PostComment comment) {
        return new PostComentDTO(
                comment.getId(),
                comment.getContent(),
                mapToCommunityProfileDTO(comment.getProfile())
        );
    }

    private CommunityProfileDTO mapToCommunityProfileDTO(CommunityProfile profile) {
        // Título destacado (si existe)
        Title featured = profile.getFeaturedTitle();
        TitleDTO featuredTitle = featured != null
                ? new TitleDTO(
                featured.getId(),
                featured.getTitle(),
                featured.getTextColor(),
                featured.getBackgroundColor(),
                featured.getCommunity().getId()
        )
                : null;

        // Rol único (ahora es un solo objeto, no lista)
        RoleDTO roleDTO = profile.getRole() != null
                ? new RoleDTO(
                profile.getRole().getId(),
                profile.getRole().getName(),
                profile.getRole().getTextColor(),
                profile.getRole().getBackgroundColor(),
                profile.getRole().getCommunity().getId()
        )
                : null;

        // Títulos
        List<TitleDTO> titleDTOs = profile.getTitles() != null
                ? profile.getTitles().stream()
                .map(title -> new TitleDTO(
                        title.getId(),
                        title.getTitle(),
                        title.getTextColor(),
                        title.getBackgroundColor(),
                        title.getCommunity().getId()
                ))
                .collect(Collectors.toList())
                : List.of();

        // Construcción del DTO final
        return new CommunityProfileDTO(
                profile.getId(),
                profile.getUsername(),
                profile.getDescription(),
                profile.getProfileImage(),
                featuredTitle,
                profile.getUser().getId(),
                profile.getCommunity().getId(),
                roleDTO,
                titleDTOs
        );
    }

}

