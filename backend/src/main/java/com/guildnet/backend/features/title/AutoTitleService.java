package com.guildnet.backend.features.title;

import com.guildnet.backend.features.like.LikeRepository;
import com.guildnet.backend.features.notification.NotificationService;
import com.guildnet.backend.features.notification.NotificationType;
import com.guildnet.backend.features.post.PostRepository;
import com.guildnet.backend.features.postcomment.PostCommentRepository;
import com.guildnet.backend.features.profile.CommunityProfile;
import com.guildnet.backend.features.profile.CommunityProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Asigna los títulos automáticos a quien cumple su regla. Se comprueba al
 * publicar, comentar y recibir un "me gusta", al crear o cambiar una regla
 * (para los miembros que ya la cumplían) y cada noche (antigüedad).
 * <p>
 * Los títulos conseguidos no se retiran solos aunque la actividad baje
 * (p. ej. si se borran publicaciones); un moderador puede quitarlos a mano.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AutoTitleService {

    private final TitleRepository titleRepository;
    private final CommunityProfileRepository profileRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final NotificationService notificationService;

    /** Comprueba los títulos automáticos de la comunidad del perfil. */
    public void evaluate(CommunityProfile profile) {
        List<Title> rules = titleRepository.findByCommunityIdAndAutoMetricIsNotNull(profile.getCommunity().getId());
        rules.forEach(title -> awardIfEarned(profile, title));
    }

    /** Aplica una regla a todos los miembros (al crearla o cambiarla). */
    public void evaluateAll(Title title) {
        if (title.getAutoMetric() == null) {
            return;
        }
        profileRepository.findByCommunityId(title.getCommunity().getId()).forEach(profile -> awardIfEarned(profile, title));
    }

    /** Cada noche: los títulos por antigüedad no dependen de ninguna acción del usuario. */
    @Scheduled(cron = "${guildnet.auto-titles.cron:0 15 4 * * *}")
    public void evaluateSeniority() {
        List<Title> rules = titleRepository.findByAutoMetric(TitleMetric.DAYS_MEMBER);
        rules.forEach(this::evaluateAll);
        if (!rules.isEmpty()) {
            log.info("Títulos por antigüedad revisados: {} reglas", rules.size());
        }
    }

    /** Valor actual de la métrica para el perfil. */
    public long progress(CommunityProfile profile, TitleMetric metric) {
        return switch (metric) {
            case POSTS -> postRepository.countByProfileId(profile.getId());
            case COMMENTS -> commentRepository.countByProfileId(profile.getId());
            case LIKES_RECEIVED -> likeRepository.countByPostProfileId(profile.getId());
            case DAYS_MEMBER -> profile.getJoinedAt() == null ? 0
                    : Duration.between(profile.getJoinedAt(), LocalDateTime.now()).toDays();
        };
    }

    private void awardIfEarned(CommunityProfile profile, Title title) {
        if (profile.getTitles().contains(title) || progress(profile, title.getAutoMetric()) < title.getAutoThreshold()) {
            return;
        }
        profile.getTitles().add(title);
        notificationService.notify(profile, null, NotificationType.ACHIEVEMENT_UNLOCKED,
                "¡Has conseguido el título \"" + title.getTitle() + "\"!");
    }
}
