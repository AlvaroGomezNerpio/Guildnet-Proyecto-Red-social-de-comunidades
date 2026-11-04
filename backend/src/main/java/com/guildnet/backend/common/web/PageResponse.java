package com.guildnet.backend.common.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.Function;

/**
 * Página de resultados con un formato JSON estable (Spring desaconseja serializar {@link Page} directamente).
 *
 * @param content       elementos de la página
 * @param page          número de página (empieza en 0)
 * @param size          tamaño de página solicitado
 * @param totalElements total de elementos
 * @param totalPages    total de páginas
 * @param last          si es la última página
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 50;

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    /** Petición de página con límites razonables (tamaño entre 1 y {@value #MAX_SIZE}). */
    public static PageRequest request(int page, int size, Sort sort) {
        return PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_SIZE), sort);
    }
}
