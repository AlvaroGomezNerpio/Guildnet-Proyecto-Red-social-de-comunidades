package com.guildnet.backend.features.post;

import com.guildnet.backend.common.exception.ConflictException;
import com.guildnet.backend.common.exception.NotFoundException;
import com.guildnet.backend.common.html.HtmlSanitizer;
import com.guildnet.backend.common.mapper.DtoMapper;
import com.guildnet.backend.common.web.PageResponse;
import com.guildnet.backend.features.category.PostCategoryDTO;
import com.guildnet.backend.features.category.PostCategoryService;
import com.guildnet.backend.features.profile.CommunityProfile;
import com.guildnet.backend.features.title.AutoTitleService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    /** Máximo de publicaciones fijadas por comunidad. */
    static final int MAX_PINNED = 3;

    private final PostRepository postRepository;
    private final PostCategoryService categoryService;
    private final AutoTitleService autoTitleService;
    private final AccessGuard accessGuard;

    /** Publica en la comunidad como el perfil del usuario autenticado (debe ser miembro). */
    public PostDTO create(PostCreateDTO dto, User user) {
        CommunityProfile author = accessGuard.requireMembership(user, dto.communityId());

        Post post = Post.builder()
                .title(dto.title().trim())
                .content(HtmlSanitizer.sanitize(dto.content()))
                .tags(cleanTags(dto.tags()))
                .category(categoryService.resolveForPost(dto.categoryId(), dto.communityId()))
                .profile(author)
                .community(author.getCommunity())
                .build();
        PostDTO created = DtoMapper.toPostDTO(postRepository.saveAndFlush(post));
        autoTitleService.evaluate(author);
        return created;
    }

    /** Lo puede editar el autor o quien tenga EDIT_POST en la comunidad. */
    public PostDTO update(Long id, PostUpdateDTO dto, User user) {
        Post post = load(id);
        accessGuard.requireAuthorOrPermission(user, post.getProfile(), PermissionType.EDIT_POST);

        post.setTitle(dto.title().trim());
        post.setContent(HtmlSanitizer.sanitize(dto.content()));
        post.getTags().clear();
        post.getTags().addAll(cleanTags(dto.tags()));
        post.setCategory(categoryService.resolveForPost(dto.categoryId(), post.getCommunity().getId()));
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
                post.getUpdatedAt(),
                post.getComments().stream().map(DtoMapper::toPostCommentDTO).toList(),
                DtoMapper.toProfileDTO(post.getProfile()),
                post.getCommunity().getId(),
                post.getPinnedAt() != null,
                PostCategoryDTO.of(post.getCategory())
        );
    }

    /** Las publicaciones se devuelven de la más reciente a la más antigua. */
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    @Transactional(readOnly = true)
    public PageResponse<PostDTO> getByProfile(Long profileId, int page, int size) {
        return PageResponse.of(
                postRepository.findByProfileId(profileId, PageResponse.request(page, size, NEWEST_FIRST)),
                DtoMapper::toPostDTO);
    }

    @Transactional(readOnly = true)
    public PageResponse<PostDTO> getByCommunity(Long communityId, Long categoryId, int page, int size) {
        var request = PageResponse.request(page, size, NEWEST_FIRST);
        return PageResponse.of(
                categoryId == null
                        ? postRepository.findByCommunityIdAndPinnedAtIsNull(communityId, request)
                        : postRepository.findByCommunityIdAndCategoryIdAndPinnedAtIsNull(communityId, categoryId, request),
                DtoMapper::toPostDTO);
    }

    /** Publicaciones fijadas de la comunidad, la más reciente primero. */
    @Transactional(readOnly = true)
    public List<PostDTO> getPinned(Long communityId) {
        return postRepository.findByCommunityIdAndPinnedAtIsNotNullOrderByPinnedAtDesc(communityId).stream()
                .map(DtoMapper::toPostDTO)
                .toList();
    }

    /** Fija la publicación en lo alto de la comunidad. Requiere PIN_POSTS; como mucho {@value #MAX_PINNED}. */
    public PostDTO pin(Long id, User user) {
        Post post = load(id);
        Long communityId = post.getCommunity().getId();
        accessGuard.requirePermission(user, communityId, PermissionType.PIN_POSTS);
        if (post.getPinnedAt() == null) {
            if (postRepository.countByCommunityIdAndPinnedAtIsNotNull(communityId) >= MAX_PINNED) {
                throw new ConflictException("Solo se pueden fijar " + MAX_PINNED + " publicaciones; desfija alguna antes");
            }
            postRepository.setPinnedAt(id, LocalDateTime.now());
        }
        return DtoMapper.toPostDTO(load(id));
    }

    public PostDTO unpin(Long id, User user) {
        Post post = load(id);
        accessGuard.requirePermission(user, post.getCommunity().getId(), PermissionType.PIN_POSTS);
        postRepository.setPinnedAt(id, null);
        return DtoMapper.toPostDTO(load(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<PostDTO> search(Long communityId, String title, List<String> tags, int page, int size) {
        return PageResponse.of(
                postRepository.findAll(PostSpecifications.search(communityId, title, tags),
                        PageResponse.request(page, size, NEWEST_FIRST)),
                DtoMapper::toPostDTO);
    }

    private Post load(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new NotFoundException("Publicación no encontrada"));
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
