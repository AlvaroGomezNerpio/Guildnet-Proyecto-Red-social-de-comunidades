package com.guildnet.backend.features.like;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByPostIdAndProfileId(Long postId, Long profileId);

    Optional<Like> findByPostIdAndProfileId(Long postId, Long profileId);

    int countByPostId(Long postId);

    @Query("SELECT l.profile.id FROM Like l WHERE l.post.id = :postId")
    List<Long> findProfileIdsByPostId(@Param("postId") Long postId);
}
