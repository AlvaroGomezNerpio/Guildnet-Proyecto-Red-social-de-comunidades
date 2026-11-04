package com.guildnet.backend.features.postcomment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostIdOrderByIdAsc(Long postId);

    long countByProfileId(Long profileId);
}
