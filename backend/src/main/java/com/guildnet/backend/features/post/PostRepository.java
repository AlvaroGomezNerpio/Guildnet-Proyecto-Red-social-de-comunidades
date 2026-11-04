package com.guildnet.backend.features.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    Page<Post> findByProfileId(Long profileId, Pageable pageable);

    /** Muro de la comunidad: las fijadas se muestran aparte, encima. */
    Page<Post> findByCommunityIdAndPinnedAtIsNull(Long communityId, Pageable pageable);

    List<Post> findByCommunityIdAndPinnedAtIsNotNullOrderByPinnedAtDesc(Long communityId);

    long countByCommunityIdAndPinnedAtIsNotNull(Long communityId);

    /**
     * Fija o desfija con una actualización directa: así no cambia {@code updatedAt}
     * y la publicación no aparece como "editada".
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.pinnedAt = :pinnedAt where p.id = :id")
    void setPinnedAt(@Param("id") Long id, @Param("pinnedAt") LocalDateTime pinnedAt);
}
