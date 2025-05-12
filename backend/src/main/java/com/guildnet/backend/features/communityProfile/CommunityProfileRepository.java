package com.guildnet.backend.features.communityProfile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityProfileRepository extends JpaRepository<CommunityProfile, Long> {
    Optional<CommunityProfile> findByUserIdAndCommunityId(Long userId, Long communityId);
    List<CommunityProfile> findByCommunityId(Long communityId);
    Optional<CommunityProfile> findByUserUsernameAndCommunityId(String username, Long communityId);
}