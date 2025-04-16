package com.guildnet.backend.features.like;

import com.guildnet.backend.features.communityProfile.CommunityProfileRepository;
import com.guildnet.backend.features.post.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommunityProfileRepository profileRepository;

    @Override
    @Transactional
    public void likePost(Long postId, Long profileId) {
        boolean alreadyLiked = likeRepository.existsByPostIdAndProfileId(postId, profileId);
        if (alreadyLiked) return;

        var post = postRepository.findById(postId).orElseThrow();
        var profile = profileRepository.findById(profileId).orElseThrow();

        Like like = new Like();
        like.setPost(post);
        like.setProfile(profile);

        likeRepository.save(like);
    }

    @Override
    @Transactional
    public void unlikePost(Long postId, Long profileId) {
        likeRepository.deleteByPostIdAndProfileId(postId, profileId);
    }

    @Override
    public int countLikesByPostId(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    @Override
    public boolean hasUserLikedPost(Long postId, Long profileId) {
        return likeRepository.existsByPostIdAndProfileId(postId, profileId);
    }

    @Override
    public List<Long> getProfileIdsWhoLikedPost(Long postId) {
        return likeRepository.findByPostId(postId).stream()
                .map(like -> like.getProfile().getId())
                .collect(Collectors.toList());
    }


}