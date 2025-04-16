package com.guildnet.backend.features.profileComment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileCommentRepository extends JpaRepository<ProfileComment, Long> {

    // Comentarios que ha recibido un perfil
    List<ProfileComment> findByTargetProfileId(Long profileId);

    // Comentarios que ha escrito un perfil (opcional si se necesita)
    List<ProfileComment> findByAuthorProfileId(Long profileId);
}
