package com.guildnet.backend.features.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {

    List<Role> findByCommunityIdOrderByIdAsc(Long communityId);

    boolean existsByCommunityIdAndNameIgnoreCase(Long communityId, String name);

    boolean existsByCommunityIdAndNameIgnoreCaseAndIdNot(Long communityId, String name, Long id);
}
