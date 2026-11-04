package com.guildnet.backend.features.postcomment;

import com.guildnet.backend.common.exception.NotFoundException;
import com.guildnet.backend.common.mapper.DtoMapper;
import com.guildnet.backend.features.profile.CommunityProfile;
import com.guildnet.backend.features.title.AutoTitleService;
import com.guildnet.backend.features.notification.NotificationService;
import com.guildnet.backend.features.notification.NotificationType;
import com.guildnet.backend.features.permission.PermissionType;
import com.guildnet.backend.features.post.Post;
import com.guildnet.backend.features.post.PostRepository;
import com.guildnet.backend.features.postcomment.dto.PostCommentCreateDTO;
import com.guildnet.backend.features.postcomment.dto.PostCommentDTO;
import com.guildnet.backend.features.postcomment.dto.PostCommentUpdateDTO;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommentService {

    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;
    private final AutoTitleService autoTitleService;
    private final AccessGuard accessGuard;

    /** Comenta como el perfil del usuario en la comunidad de la publicación. */
    public PostCommentDTO create(PostCommentCreateDTO dto, User user) {
        Post post = postRepository.findById(dto.postId())
                .orElseThrow(() -> new NotFoundException("Publicación no encontrada"));
        CommunityProfile author = accessGuard.requireMembership(user, post.getCommunity().getId());

        PostComment comment = commentRepository.save(PostComment.builder()
                .content(dto.content().trim())
                .post(post)
                .profile(author)
                .build());

        notificationService.notify(post.getProfile(), author, NotificationType.COMMENT_POST, "Ha comentado tu publicación.");
        commentRepository.flush();
        autoTitleService.evaluate(author);
        return DtoMapper.toPostCommentDTO(comment);
    }

    /** Lo puede editar el autor o quien tenga EDIT_COMMENTS. */
    public PostCommentDTO update(Long id, PostCommentUpdateDTO dto, User user) {
        PostComment comment = load(id);
        accessGuard.requireAuthorOrPermission(user, comment.getProfile(), PermissionType.EDIT_COMMENTS);
        comment.setContent(dto.content().trim());
        return DtoMapper.toPostCommentDTO(comment);
    }

    /** Lo puede borrar el autor o quien tenga DELETE_COMMENTS. */
    public void delete(Long id, User user) {
        PostComment comment = load(id);
        accessGuard.requireAuthorOrPermission(user, comment.getProfile(), PermissionType.DELETE_COMMENTS);
        comment.getPost().getComments().remove(comment);
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<PostCommentDTO> getByPost(Long postId) {
        return commentRepository.findByPostIdOrderByIdAsc(postId).stream().map(DtoMapper::toPostCommentDTO).toList();
    }

    private PostComment load(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new NotFoundException("Comentario no encontrado"));
    }
}
