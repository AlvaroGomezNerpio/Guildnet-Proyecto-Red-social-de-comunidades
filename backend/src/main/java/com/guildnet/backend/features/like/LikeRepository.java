package com.guildnet.backend.features.like;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByPostIdAndProfileId(Long postId, Long profileId);
    void deleteByPostIdAndProfileId(Long postId, Long profileId);
    int countByPostId(Long postId);
    List<Like> findByPostId(Long postId);
}
