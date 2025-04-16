package com.guildnet.backend.features.postComment;

import com.guildnet.backend.features.communityProfile.CommunityProfile;
import com.guildnet.backend.features.communityProfile.CommunityProfileRepository;
import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.post.PostRepository;
import com.guildnet.backend.features.postComment.dto.PostComentDTO;
import com.guildnet.backend.features.postComment.dto.PostCommentCreateDTO;
import com.guildnet.backend.features.postComment.dto.PostCommentUpdateDTO;
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

        return new CommunityProfileDTO(
                profile.getId(),
                profile.getUsername(),
                profile.getDescription(),
                profile.getProfileImage(),
                featuredTitle,
                profile.getUser().getId(),
                profile.getCommunity().getId(),
                profile.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()),
                profile.getTitles().stream().map(t -> t.getTitle()).collect(Collectors.toList())
        );
    }
}