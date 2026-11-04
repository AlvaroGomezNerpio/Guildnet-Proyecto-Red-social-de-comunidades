-- Categorías oficiales de publicaciones de cada comunidad ("Guía", "Pregunta", "Evento"...).
CREATE TABLE post_categories (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    community_id     BIGINT      NOT NULL,
    name             VARCHAR(30) NOT NULL,
    text_color       VARCHAR(7)  NOT NULL,
    background_color VARCHAR(7)  NOT NULL,
    CONSTRAINT pk_post_categories PRIMARY KEY (id),
    CONSTRAINT uk_post_categories_name UNIQUE (community_id, name),
    CONSTRAINT fk_post_categories_community FOREIGN KEY (community_id) REFERENCES communities (id) ON DELETE CASCADE
);

-- Categoría de cada publicación (opcional). Si se borra la categoría, la publicación se queda sin ella.
ALTER TABLE posts ADD COLUMN category_id BIGINT NULL;
ALTER TABLE posts ADD CONSTRAINT fk_posts_category FOREIGN KEY (category_id) REFERENCES post_categories (id) ON DELETE SET NULL;
CREATE INDEX ix_posts_community_category ON posts (community_id, category_id, created_at);
