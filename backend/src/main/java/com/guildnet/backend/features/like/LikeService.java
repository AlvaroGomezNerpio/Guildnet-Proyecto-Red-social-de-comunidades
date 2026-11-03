package com.guildnet.backend.features.like;

import com.guildnet.backend.common.exception.BadRequestException;
import com.guildnet.backend.common.exception.NotFoundException;
import com.guildnet.backend.features.communityProfile.CommunityProfile;
import com.guildnet.backend.features.notification.NotificationService;
import com.guildnet.backend.features.notification.NotificationType;
import com.guildnet.backend.features.post.Post;
import com.guildnet.backend.features.post.PostRepository;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;
    private final AccessGuard accessGuard;

    /** Da like como el perfil indicado, que debe ser del usuario y de la comunidad del post. */
    public void like(Long postId, Long profileId, User user) {
        Post post = loadPost(postId);
        CommunityProfile profile = requireOwnProfileInCommunity(user, profileId, post);

        if (likeRepository.existsByPostIdAndProfileId(postId, profileId)) {
            return;
        }
        likeRepository.save(Like.builder().post(post).profile(profile).build());
        notificationService.notify(post.getProfile(), profile, NotificationType.LIKE_POST, "Ha dado like a tu publicación.");
    }

    public void unlike(Long postId, Long profileId, User user) {
        Post post = loadPost(postId);
        requireOwnProfileInCommunity(user, profileId, post);
        likeRepository.findByPostIdAndProfileId(postId, profileId).ifPresent(like -> {
            post.getLikes().remove(like);
            likeRepository.delete(like);
        });
    }

    @Transactional(readOnly = true)
    public int count(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    @Transactional(readOnly = true)
    public boolean hasLiked(Long postId, Long profileId) {
        return likeRepository.existsByPostIdAndProfileId(postId, profileId);
    }

    @Transactional(readOnly = true)
    public List<Long> getProfileIds(Long postId) {
        return likeRepository.findProfileIdsByPostId(postId);
    }

    private CommunityProfile requireOwnProfileInCommunity(User user, Long profileId, Post post) {
        CommunityProfile profile = accessGuard.requireOwnProfile(user, profileId);
        if (!profile.getCommunity().getId().equals(post.getCommunity().getId())) {
            throw new BadRequestException("El perfil no pertenece a la comunidad de la publicación");
        }
        return profile;
    }

    private Post loadPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Publicación no encontrada"));
    }
}
