package com.guildnet.backend.features.community;

import com.guildnet.backend.common.persistence.LikePatterns;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/** Filtros de búsqueda de comunidades por nombre y etiquetas (coincidencia parcial, sin distinguir mayúsculas). */
final class CommunitySpecifications {

    private CommunitySpecifications() {
    }

    static Specification<Community> search(String name, List<String> tags) {
        return (root, query, cb) -> {
            query.distinct(true);
            Predicate predicate = cb.conjunction();

            if (name != null && !name.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("name")), LikePatterns.contains(name), LikePatterns.ESCAPE));
            }

            List<String> cleanTags = tags == null ? List.of() : tags.stream().filter(t -> t != null && !t.isBlank()).toList();
            if (!cleanTags.isEmpty()) {
                Join<Community, String> tagJoin = root.join("tags");
                predicate = cb.and(predicate, cb.or(cleanTags.stream()
                        .map(tag -> cb.like(cb.lower(tagJoin), LikePatterns.contains(tag), LikePatterns.ESCAPE))
                        .toArray(Predicate[]::new)));
            }
            return predicate;
        };
    }
}
