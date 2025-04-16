package com.guildnet.backend.features.postComment;

import com.guildnet.backend.features.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    void deleteByPostId(Long postId);

    List<PostComment> findByPostId(Long postId);
}

