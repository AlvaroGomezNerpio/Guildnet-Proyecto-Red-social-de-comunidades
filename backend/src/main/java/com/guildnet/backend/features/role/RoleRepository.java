package com.guildnet.backend.features.role;

import com.guildnet.backend.features.Community.Community;
import com.guildnet.backend.features.role.dto.RoleDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    List<Role> findByCommunity(Community community);

}
