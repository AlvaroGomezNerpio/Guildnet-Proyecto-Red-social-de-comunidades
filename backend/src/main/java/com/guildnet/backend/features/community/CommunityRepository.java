package com.guildnet.backend.features.community;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Community, Long>, JpaSpecificationExecutor<Community> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("SELECT cp.community FROM CommunityProfile cp WHERE cp.user.id = :userId")
    List<Community> findCommunitiesByUserId(@Param("userId") Long userId);

    /** Comunidades que comparten alguna etiqueta con el usuario y a las que aún no pertenece. */
    @Query("""
            SELECT DISTINCT c FROM Community c JOIN c.tags t
            WHERE LOWER(t) IN :tags
              AND NOT EXISTS (
                  SELECT 1 FROM CommunityProfile cp WHERE cp.community = c AND cp.user.id = :userId
              )
            """)
    List<Community> findSuggestedCommunities(@Param("tags") List<String> tags, @Param("userId") Long userId);

    @Query("""
            SELECT c FROM Community c
            LEFT JOIN c.communityProfiles cp
            GROUP BY c
            ORDER BY COUNT(cp) DESC
            """)
    List<Community> findCommunitiesOrderBySubscribersDesc();
}
