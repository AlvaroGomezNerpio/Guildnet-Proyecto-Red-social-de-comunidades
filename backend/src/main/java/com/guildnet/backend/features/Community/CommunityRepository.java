package com.guildnet.backend.features.Community;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {

    List<Community> findByNameContainingIgnoreCase(String name);

    List<Community> findByTagsContainingIgnoreCase(String tag);

    @Query("SELECT c FROM Community c JOIN c.tags t WHERE " +
            "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:tag IS NULL OR t = :tag)")
    List<Community> searchByNameAndTag(@Param("name") String name, @Param("tag") String tag);

    @Query("SELECT cp.community FROM CommunityProfile cp WHERE cp.user.id = :userId")
    List<Community> findCommunitiesByUserId(@Param("userId") Long userId);

    @Query(value = """
                SELECT DISTINCT c.* FROM community c
                JOIN community_tags ct ON c.id = ct.community_id
                WHERE ct.tags IN (:tags)
                AND c.id NOT IN (
                    SELECT cp.community_id FROM community_profile cp WHERE cp.user_id = :userId
                )
            """, nativeQuery = true)
    List<Community> findSuggestedCommunities(@Param("tags") List<String> tags, @Param("userId") Long userId);

    @Query("""
                SELECT c FROM Community c
                LEFT JOIN c.communityProfiles cp
                GROUP BY c
                ORDER BY COUNT(cp) DESC
            """)
    List<Community> findCommunitiesOrderBySubscribersDesc();


}
