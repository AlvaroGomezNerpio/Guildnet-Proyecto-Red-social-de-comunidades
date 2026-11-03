package com.guildnet.backend.features.profileComment;

import com.guildnet.backend.common.exception.BadRequestException;
import com.guildnet.backend.common.exception.NotFoundException;
import com.guildnet.backend.common.mapper.DtoMapper;
import com.guildnet.backend.features.communityProfile.CommunityProfile;
import com.guildnet.backend.features.communityProfile.CommunityProfileRepository;
import com.guildnet.backend.features.notification.NotificationService;
import com.guildnet.backend.features.notification.NotificationType;
import com.guildnet.backend.features.permission.PermissionType;
import com.guildnet.backend.features.profileComment.dto.ProfileCommentCreateUpdateDTO;
import com.guildnet.backend.features.profileComment.dto.ProfileCommentDTO;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileCommentService {

    private final ProfileCommentRepository commentRepository;
    private final CommunityProfileRepository profileRepository;
    private final NotificationService notificationService;
    private final AccessGuard accessGuard;

    /** El autor debe ser un perfil propio y de la misma comunidad que el perfil comentado. */
    public ProfileCommentDTO create(Long authorProfileId, Long targetProfileId, ProfileCommentCreateUpdateDTO dto, User user) {
        CommunityProfile author = accessGuard.requireOwnProfile(user, authorProfileId);
        CommunityProfile target = profileRepository.findById(targetProfileId)
                .orElseThrow(() -> new NotFoundException("Perfil no encontrado"));
        if (!author.getCommunity().getId().equals(target.getCommunity().getId())) {
            throw new BadRequestException("Solo puedes comentar perfiles de tu misma comunidad");
        }

        ProfileComment comment = commentRepository.save(ProfileComment.builder()
                .content(dto.content().trim())
                .authorProfile(author)
                .targetProfile(target)
                .build());

        notificationService.notify(target, author, NotificationType.COMMENT_PROFILE, "Ha comentado tu perfil.");
        return toDTO(comment);
    }

    /** Lo puede editar el autor o quien tenga EDIT_COMMENTS. */
    public ProfileCommentDTO update(Long commentId, ProfileCommentCreateUpdateDTO dto, User user) {
        ProfileComment comment = load(commentId);
        accessGuard.requireAuthorOrPermission(user, comment.getAuthorProfile(), PermissionType.EDIT_COMMENTS);
        comment.setContent(dto.content().trim());
        return toDTO(comment);
    }

    /** Lo puede borrar el autor, el dueño del perfil comentado o quien tenga DELETE_COMMENTS. */
    public void delete(Long commentId, User user) {
        ProfileComment comment = load(commentId);
        if (!accessGuard.isOwnProfile(user, comment.getTargetProfile())) {
            accessGuard.requireAuthorOrPermission(user, comment.getAuthorProfile(), PermissionType.DELETE_COMMENTS);
        }
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<ProfileCommentDTO> getByTarget(Long targetProfileId) {
        return commentRepository.findByTargetProfileIdOrderByIdDesc(targetProfileId).stream()
                .map(ProfileCommentService::toDTO)
                .toList();
    }

    private ProfileComment load(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new NotFoundException("Comentario no encontrado"));
    }

    private static ProfileCommentDTO toDTO(ProfileComment comment) {
        return new ProfileCommentDTO(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                DtoMapper.toProfileDTO(comment.getAuthorProfile()),
                DtoMapper.toProfileDTO(comment.getTargetProfile())
        );
    }
}
