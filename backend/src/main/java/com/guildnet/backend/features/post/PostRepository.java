package com.guildnet.backend.features.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    Page<Post> findByProfileId(Long profileId, Pageable pageable);

    Page<Post> findByCommunityId(Long communityId, Pageable pageable);
}
