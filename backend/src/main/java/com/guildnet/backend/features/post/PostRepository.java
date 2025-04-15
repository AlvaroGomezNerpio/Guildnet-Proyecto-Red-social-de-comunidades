package com.guildnet.backend.features.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByProfileId(Long profileId);
    List<Post> findByCommunityId(Long comumnunityId);

}
