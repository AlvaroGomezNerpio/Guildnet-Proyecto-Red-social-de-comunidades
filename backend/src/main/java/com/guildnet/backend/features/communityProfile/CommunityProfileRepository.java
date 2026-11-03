package com.guildnet.backend.features.communityProfile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommunityProfileRepository extends JpaRepository<CommunityProfile, Long> {

    Optional<CommunityProfile> findByUserIdAndCommunityId(Long userId, Long communityId);

    boolean existsByUserIdAndCommunityId(Long userId, Long communityId);

    List<CommunityProfile> findByCommunityId(Long communityId);

    List<CommunityProfile> findByRoleId(Long roleId);

    List<CommunityProfile> findByFeaturedTitleId(Long titleId);
}
