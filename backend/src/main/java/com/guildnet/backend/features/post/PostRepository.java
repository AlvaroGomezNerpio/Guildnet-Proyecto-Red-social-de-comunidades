package com.guildnet.backend.features.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    List<Post> findByProfileIdOrderByIdDesc(Long profileId);

    List<Post> findByCommunityIdOrderByIdDesc(Long communityId);
}
