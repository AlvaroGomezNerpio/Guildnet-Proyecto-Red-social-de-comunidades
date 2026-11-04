package com.guildnet.backend.features.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {

    List<PostCategory> findByCommunityIdOrderByIdAsc(Long communityId);

    long countByCommunityId(Long communityId);

    boolean existsByCommunityIdAndNameIgnoreCase(Long communityId, String name);

    boolean existsByCommunityIdAndNameIgnoreCaseAndIdNot(Long communityId, String name, Long id);
}
