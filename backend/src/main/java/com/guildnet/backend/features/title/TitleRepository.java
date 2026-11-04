package com.guildnet.backend.features.title;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TitleRepository extends JpaRepository<Title, Long> {
    List<Title> findByCommunityId(Long communityId);

    List<Title> findByCommunityIdAndAutoMetricIsNotNull(Long communityId);

    List<Title> findByAutoMetric(TitleMetric metric);

    boolean existsByCommunityIdAndTitleIgnoreCase(Long communityId, String title);

    boolean existsByCommunityIdAndTitleIgnoreCaseAndIdNot(Long communityId, String title, Long id);
}

