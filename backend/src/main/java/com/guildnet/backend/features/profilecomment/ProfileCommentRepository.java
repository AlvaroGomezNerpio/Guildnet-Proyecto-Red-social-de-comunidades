package com.guildnet.backend.features.profilecomment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileCommentRepository extends JpaRepository<ProfileComment, Long> {

    List<ProfileComment> findByTargetProfileIdOrderByIdDesc(Long targetProfileId);
}
