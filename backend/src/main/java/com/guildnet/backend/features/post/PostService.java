package com.guildnet.backend.features.post;

import com.guildnet.backend.common.exception.NotFoundException;
import com.guildnet.backend.common.html.HtmlSanitizer;
import com.guildnet.backend.common.mapper.DtoMapper;
import com.guildnet.backend.features.communityProfile.CommunityProfile;
import com.guildnet.backend.features.permission.PermissionType;
import com.guildnet.backend.features.post.dto.PostCreateDTO;
import com.guildnet.backend.features.post.dto.PostDTO;
import com.guildnet.backend.features.post.dto.PostDetailDTO;
import com.guildnet.backend.features.post.dto.PostUpdateDTO;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final AccessGuard accessGuard;

    /** Publica en la comunidad como el perfil del usuario autenticado (debe ser miembro). */
    public PostDTO create(PostCreateDTO dto, User user) {
        CommunityProfile author = accessGuard.requireMembership(user, dto.communityId());

        Post post = Post.builder()
                .title(dto.title().trim())
                .content(HtmlSanitizer.sanitize(dto.content()))
                .tags(cleanTags(dto.tags()))
                .profile(author)
                .community(author.getCommunity())
                .build();
        return DtoMapper.toPostDTO(postRepository.saveAndFlush(post));
    }

    /** Lo puede editar el autor o quien tenga EDIT_POST en la comunidad. */
    public PostDTO update(Long id, PostUpdateDTO dto, User user) {
        Post post = load(id);
        accessGuard.requireAuthorOrPermission(user, post.getProfile(), PermissionType.EDIT_POST);

        post.setTitle(dto.title().trim());
        post.setContent(HtmlSanitizer.sanitize(dto.content()));
        post.getTags().clear();
        post.getTags().addAll(cleanTags(dto.tags()));
        return DtoMapper.toPostDTO(post);
    }

    /** Lo puede borrar el autor o quien tenga DELETE_POST en la comunidad. */
    public void delete(Long id, User user) {
        Post post = load(id);
        accessGuard.requireAuthorOrPermission(user, post.getProfile(), PermissionType.DELETE_POST);
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public PostDetailDTO getById(Long id) {
        Post post = load(id);
        return new PostDetailDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                List.copyOf(post.getTags()),
                post.getCreatedAt(),
                post.getComments().stream().map(DtoMapper::toPostCommentDTO).toList(),
                DtoMapper.toProfileDTO(post.getProfile()),
                post.getCommunity().getId()
        );
    }

    @Transactional(readOnly = true)
    public List<PostDTO> getByProfile(Long profileId) {
        return toDTOs(postRepository.findByProfileIdOrderByIdDesc(profileId));
    }

    @Transactional(readOnly = true)
    public List<PostDTO> getByCommunity(Long communityId) {
        return toDTOs(postRepository.findByCommunityIdOrderByIdDesc(communityId));
    }

    @Transactional(readOnly = true)
    public List<PostDTO> search(Long communityId, String title, List<String> tags) {
        return toDTOs(postRepository.findAll(
                PostSpecifications.search(communityId, title, tags),
                Sort.by(Sort.Direction.DESC, "id")));
    }

    private Post load(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new NotFoundException("Publicación no encontrada"));
    }

    private static List<PostDTO> toDTOs(List<Post> posts) {
        return posts.stream().map(DtoMapper::toPostDTO).toList();
    }

    private static List<String> cleanTags(List<String> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(tags.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(String::trim)
                .distinct()
                .toList());
    }
}
