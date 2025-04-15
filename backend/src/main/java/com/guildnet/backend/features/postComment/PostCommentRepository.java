package com.guildnet.backend.features.postComment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    void deleteByPostId(Long postId);
}

