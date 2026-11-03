package com.guildnet.backend.features.post;

import com.guildnet.backend.common.persistence.LikePatterns;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/** Búsqueda de publicaciones de una comunidad por título y/o etiquetas (coincidencia parcial). */
final class PostSpecifications {

    private PostSpecifications() {
    }

    static Specification<Post> search(Long communityId, String title, List<String> tags) {
        return (root, query, cb) -> {
            query.distinct(true);
            Predicate predicate = cb.equal(root.get("community").get("id"), communityId);

            if (title != null && !title.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("title")), LikePatterns.contains(title), LikePatterns.ESCAPE));
            }

            List<String> cleanTags = tags == null ? List.of() : tags.stream().filter(t -> t != null && !t.isBlank()).toList();
            if (!cleanTags.isEmpty()) {
                Join<Post, String> tagJoin = root.join("tags");
                predicate = cb.and(predicate, cb.or(cleanTags.stream()
                        .map(tag -> cb.like(cb.lower(tagJoin), LikePatterns.contains(tag), LikePatterns.ESCAPE))
                        .toArray(Predicate[]::new)));
            }
            return predicate;
        };
    }
}
