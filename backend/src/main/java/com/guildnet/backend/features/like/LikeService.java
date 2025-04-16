package com.guildnet.backend.features.like;

import java.util.List;

public interface LikeService {

    void likePost(Long postId, Long profileId);

    void unlikePost(Long postId, Long profileId);

    int countLikesByPostId(Long postId);

    boolean hasUserLikedPost(Long postId, Long profileId);

    List<Long> getProfileIdsWhoLikedPost(Long postId);

}

