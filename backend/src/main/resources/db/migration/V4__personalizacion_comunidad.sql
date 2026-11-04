-- Personalización de las comunidades: identidad, enlaces, normas y publicaciones fijadas.

-- Cómo se llama a los miembros ("Entrenadores", "Fotógrafos"...). Sin valor: "miembros".
ALTER TABLE communities ADD COLUMN members_label VARCHAR(30) NULL;
-- Mensaje que recibe quien se une (sustituye al "¡Bienvenido a ...!" genérico).
ALTER TABLE communities ADD COLUMN welcome_message VARCHAR(255) NULL;
-- Tipografía de los títulos: default, serif, rounded, pixel o hand.
ALTER TABLE communities ADD COLUMN heading_font VARCHAR(20) NOT NULL DEFAULT 'default';
-- Si es true, para unirse hay que aceptar las normas.
ALTER TABLE communities ADD COLUMN require_rules_acceptance BOOLEAN NOT NULL DEFAULT FALSE;

-- Enlaces de la comunidad (Discord, web, redes...), en el orden en que se muestran.
CREATE TABLE community_links (
    community_id BIGINT       NOT NULL,
    sort_order   INT          NOT NULL,
    label        VARCHAR(40)  NOT NULL,
    url          VARCHAR(500) NOT NULL,
    CONSTRAINT pk_community_links PRIMARY KEY (community_id, sort_order),
    CONSTRAINT fk_community_links_community FOREIGN KEY (community_id) REFERENCES communities (id) ON DELETE CASCADE
);

-- Publicaciones fijadas: momento en que se fijaron (NULL si no lo están).
ALTER TABLE posts ADD COLUMN pinned_at DATETIME(6) NULL;
CREATE INDEX ix_posts_community_pinned ON posts (community_id, pinned_at);

-- Nuevo permiso para fijar publicaciones; los roles "Líder" existentes lo reciben.
INSERT INTO permissions (name, description) VALUES ('PIN_POSTS', 'Fijar publicaciones en la comunidad');
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.name = 'PIN_POSTS' WHERE r.name = 'Líder';
