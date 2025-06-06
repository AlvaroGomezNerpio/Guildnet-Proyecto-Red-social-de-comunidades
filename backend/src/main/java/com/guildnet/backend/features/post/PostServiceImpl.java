package com.guildnet.backend.features.post;

import com.guildnet.backend.features.Community.Community;
import com.guildnet.backend.features.Community.CommunityRepository;
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
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CommunityProfileRepository profileRepository;
    private final CommunityRepository communityRepository;

    @Override
    public PostDTO createPost(PostCreateDTO dto) {
        CommunityProfile profile = profileRepository.findById(dto.getProfileId())
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));
        Community community = communityRepository.findById(dto.getCommunityId())
                .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));

        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setTags(dto.getTags());
        post.setProfile(profile);
        post.setCommunity(community);

        Post saved = postRepository.save(post);
        return mapToPostDTO(saved);
    }

    @Override
    public PostDTO updatePost(Long id, PostCreateDTO dto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setTags(dto.getTags());

        return mapToPostDTO(postRepository.save(post));
    }

    @Override
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));

        post.getComments().clear();
        postRepository.delete(post);
    }

    @Override
    public PostDetailDTO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));
        return mapToPostDetailDTO(post);
    }

    @Override
    public List<PostDTO> getPostsByProfileId(Long profileId) {
        return postRepository.findByProfileId(profileId).stream()
                .map(this::mapToPostDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> getPostsByCommunityId(Long communityId) {
        return postRepository.findByCommunityId(communityId).stream()
                .map(this::mapToPostDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> searchPostsByTitleInCommunity(Long communityId, String title) {
        return postRepository.findByCommunityId(communityId).stream()
                .filter(p -> p.getTitle().toLowerCase().contains(title.toLowerCase()))
                .map(this::mapToPostDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> searchByTagsInCommunity(Long communityId, List<String> tags) {
        return postRepository.findByCommunityId(communityId).stream()
                .filter(p -> p.getTags() != null && tags.stream().anyMatch(
                        inputTag -> p.getTags().stream().anyMatch(
                                t -> t.toLowerCase().contains(inputTag.toLowerCase())
                        )
                ))
                .map(this::mapToPostDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> searchByTitleAndTagsInCommunity(Long communityId, String title, List<String> tags) {
        return postRepository.findByCommunityId(communityId).stream()
                .filter(p -> p.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(p -> p.getTags() != null && tags.stream().anyMatch(
                        inputTag -> p.getTags().stream().anyMatch(
                                t -> t.toLowerCase().contains(inputTag.toLowerCase())
                        )
                ))
                .map(this::mapToPostDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> getAllPostsInCommunity(Long communityId) {
        return postRepository.findByCommunityId(communityId).stream()
                .map(this::mapToPostDTO)
                .collect(Collectors.toList());
    }


    private PostDTO mapToPostDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setTags(post.getTags());
        dto.setLikes(post.getLikes() != null ? post.getLikes().size() : 0);
        dto.setComents(post.getComments() != null ? post.getComments().size() : 0);
        dto.setCommunityId(post.getCommunity().getId());
        dto.setCommunityProfile(mapToCommunityProfileDTO(post.getProfile()));
        return dto;
    }

    private PostDetailDTO mapToPostDetailDTO(Post post) {
        PostDetailDTO dto = new PostDetailDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setTags(post.getTags());
        dto.setCommunityId(post.getCommunity().getId());
        dto.setCommunityProfile(mapToCommunityProfileDTO(post.getProfile()));

        List<PostComentDTO> commentDTOs = post.getComments().stream()
                .map(comment -> {
                    PostComentDTO dtoComment = new PostComentDTO();
                    dtoComment.setId(comment.getId());
                    dtoComment.setContent(comment.getContent());
                    dtoComment.setCommunityProfile(mapToCommunityProfileDTO(comment.getProfile()));
                    return dtoComment;
                })
                .collect(Collectors.toList());

        dto.setPostComment(commentDTOs);
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

        // Título destacado
        if (profile.getFeaturedTitle() != null) {
            dto.setFeaturedTitle(mapToTitleDTO(profile.getFeaturedTitle()));
        }

        // Rol (único)
        if (profile.getRole() != null) {
            dto.setRol(mapToRoleDTO(profile.getRole())); // ✅ CORRECTO
        } else {
            dto.setRol(null); // también válido
        }

        // Títulos
        if (profile.getTitles() != null) {
            dto.setTitles(profile.getTitles().stream()
                    .map(this::mapToTitleDTO)
                    .toList());
        } else {
            dto.setTitles(List.of());
        }

        return dto;
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


