package com.guildnet.backend.features.Community;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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


}
