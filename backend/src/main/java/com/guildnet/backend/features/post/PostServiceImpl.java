package com.guildnet.backend.features.post;

import com.guildnet.backend.features.communityProfile.CommunityProfile;
import com.guildnet.backend.features.communityProfile.CommunityProfileRepository;
import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.post.dto.PostCreateDTO;
import com.guildnet.backend.features.post.dto.PostDTO;
import com.guildnet.backend.features.post.dto.PostDetailDTO;
import com.guildnet.backend.features.post.dto.PostUpdateDTO;
import com.guildnet.backend.features.postComment.PostComment;
import com.guildnet.backend.features.postComment.PostCommentRepository;
import com.guildnet.backend.features.postComment.dto.PostComentDTO;
import com.guildnet.backend.features.role.Role;
import com.guildnet.backend.features.title.Title;
import com.guildnet.backend.features.title.dto.TitleDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CommunityProfileRepository communityProfileRepository;
    private final PostCommentRepository postCommentRepository;

    @Override
    public PostDTO createPost(PostCreateDTO postCreateDTO) {
        CommunityProfile profile = communityProfileRepository.findById(postCreateDTO.getProfileId())
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        Post post = new Post();
        post.setTitle(postCreateDTO.getTitle());
        post.setContent(postCreateDTO.getContent());
        post.setTags(postCreateDTO.getTags());
        post.setProfile(profile);

        Post saved = postRepository.save(post);
        return mapToPostDTO(saved);
    }

    @Override
    public List<PostDTO> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::mapToPostDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PostDetailDTO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));

        return mapToPostDetailDTO(post);
    }

    @Override
    public PostDTO updatePost(Long postId, PostUpdateDTO updateDTO) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));

        if (updateDTO.getTitle() != null) {
            post.setTitle(updateDTO.getTitle());
        }

        if (updateDTO.getContent() != null) {
            post.setContent(updateDTO.getContent());
        }

        if (updateDTO.getTags() != null) {
            post.setTags(updateDTO.getTags());
        }

        Post updatedPost = postRepository.save(post);
        return mapToPostDTO(updatedPost);
    }


    @Override
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));

        // Primero eliminamos los comentarios asociados al post
        postCommentRepository.deleteByPostId(postId);

        // Luego eliminamos el post
        postRepository.delete(post);
    }


    private PostDTO mapToPostDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setTags(post.getTags());
        dto.setLikes(post.getLikes() != null ? post.getLikes().size() : 0);
        dto.setCommunityProfile(mapToCommunityProfileDTO(post.getProfile()));
        return dto;
    }

    private PostDetailDTO mapToPostDetailDTO(Post post) {
        PostDetailDTO dto = new PostDetailDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setTags(post.getTags());

        // Si quieres pasar todos los comentarios en vez de uno, puedes adaptar el DTO
        if (!post.getComments().isEmpty()) {
            PostComment comment = post.getComments().get(0);
            dto.setPostComment(mapToPostCommentDTO(comment));
        }

        dto.setCommunityProfile(mapToCommunityProfileDTO(post.getProfile()));
        return dto;
    }

    private CommunityProfileDTO mapToCommunityProfileDTO(CommunityProfile profile) {
        CommunityProfileDTO dto = new CommunityProfileDTO();
        dto.setId(profile.getId());
        dto.setUsername(profile.getUsername());
        dto.setDescription(profile.getDescription());
        dto.setProfileImage(profile.getProfileImage());
        dto.setUserId(profile.getUser().getId());
        dto.setCommunityId(profile.getCommunity().getId());

        dto.setFeaturedTitle(profile.getFeaturedTitle() != null ? new TitleDTO(
                profile.getFeaturedTitle().getId(),
                profile.getFeaturedTitle().getTitle(),
                profile.getFeaturedTitle().getTextColor(),
                profile.getFeaturedTitle().getBackgroundColor(),
                profile.getFeaturedTitle().getCommunity().getId()) : null);

        dto.setRoles(profile.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));

        dto.setTitles(profile.getTitles().stream()
                .map(Title::getTitle)
                .collect(Collectors.toList()));

        return dto;
    }

    private PostComentDTO mapToPostCommentDTO(PostComment comment) {
        PostComentDTO dto = new PostComentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCommunityProfile(mapToCommunityProfileDTO(comment.getProfile()));
        return dto;
    }
}
