-- =====================================================================
-- GuildNet - esquema inicial
-- Compatible con MySQL 8 y con H2 en modo MySQL (tests).
-- Las claves foráneas borran en cascada el contenido dependiente, de modo
-- que borrar un usuario, una comunidad o un perfil no deja datos huérfanos.
-- =====================================================================

-- ---------------------------------------------------------------- Usuarios
CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    display_name  VARCHAR(50)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    profile_image VARCHAR(500),
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE user_tags (
    user_id BIGINT      NOT NULL,
    tag     VARCHAR(30) NOT NULL,
    CONSTRAINT fk_user_tags_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX ix_user_tags_tag ON user_tags (tag);

-- ---------------------------------------------------------------- Comunidades
CREATE TABLE communities (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    description LONGTEXT,
    rules       LONGTEXT,
    image       VARCHAR(500),
    banner      VARCHAR(500),
    owner_id    BIGINT       NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    CONSTRAINT pk_communities PRIMARY KEY (id),
    CONSTRAINT uk_communities_name UNIQUE (name),
    CONSTRAINT fk_communities_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE community_tags (
    community_id BIGINT      NOT NULL,
    tag          VARCHAR(30) NOT NULL,
    CONSTRAINT fk_community_tags_community FOREIGN KEY (community_id) REFERENCES communities (id) ON DELETE CASCADE
);
CREATE INDEX ix_community_tags_tag ON community_tags (tag);

-- ---------------------------------------------------------------- Permisos, roles y títulos
CREATE TABLE permissions (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255) NOT NULL,
    CONSTRAINT pk_permissions PRIMARY KEY (id),
    CONSTRAINT uk_permissions_name UNIQUE (name)
);

CREATE TABLE roles (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    community_id     BIGINT      NOT NULL,
    name             VARCHAR(50) NOT NULL,
    text_color       VARCHAR(7)  NOT NULL,
    background_color VARCHAR(7)  NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uk_roles_community_name UNIQUE (community_id, name),
    CONSTRAINT fk_roles_community FOREIGN KEY (community_id) REFERENCES communities (id) ON DELETE CASCADE
);

CREATE TABLE role_permissions (
    id            BIGINT NOT NULL AUTO_INCREMENT,
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (id),
    CONSTRAINT uk_role_permissions UNIQUE (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

CREATE TABLE titles (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    community_id     BIGINT      NOT NULL,
    title            VARCHAR(50) NOT NULL,
    text_color       VARCHAR(7)  NOT NULL,
    background_color VARCHAR(7)  NOT NULL,
    CONSTRAINT pk_titles PRIMARY KEY (id),
    CONSTRAINT uk_titles_community_title UNIQUE (community_id, title),
    CONSTRAINT fk_titles_community FOREIGN KEY (community_id) REFERENCES communities (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------- Perfiles dentro de cada comunidad
CREATE TABLE community_profiles (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    user_id           BIGINT      NOT NULL,
    community_id      BIGINT      NOT NULL,
    username          VARCHAR(50) NOT NULL,
    description       LONGTEXT,
    profile_image     VARCHAR(500),
    role_id           BIGINT,
    featured_title_id BIGINT,
    joined_at         DATETIME(6) NOT NULL,
    CONSTRAINT pk_community_profiles PRIMARY KEY (id),
    CONSTRAINT uk_profiles_user_community UNIQUE (user_id, community_id),
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_profiles_community FOREIGN KEY (community_id) REFERENCES communities (id) ON DELETE CASCADE,
    CONSTRAINT fk_profiles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE SET NULL,
    CONSTRAINT fk_profiles_featured_title FOREIGN KEY (featured_title_id) REFERENCES titles (id) ON DELETE SET NULL
);
CREATE INDEX ix_profiles_community ON community_profiles (community_id);

CREATE TABLE profile_titles (
    profile_id BIGINT NOT NULL,
    title_id   BIGINT NOT NULL,
    CONSTRAINT pk_profile_titles PRIMARY KEY (profile_id, title_id),
    CONSTRAINT fk_profile_titles_profile FOREIGN KEY (profile_id) REFERENCES community_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_profile_titles_title FOREIGN KEY (title_id) REFERENCES titles (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------- Publicaciones
CREATE TABLE posts (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    community_id BIGINT       NOT NULL,
    profile_id   BIGINT       NOT NULL,
    title        VARCHAR(200) NOT NULL,
    content      LONGTEXT     NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NOT NULL,
    CONSTRAINT pk_posts PRIMARY KEY (id),
    CONSTRAINT fk_posts_community FOREIGN KEY (community_id) REFERENCES communities (id) ON DELETE CASCADE,
    CONSTRAINT fk_posts_profile FOREIGN KEY (profile_id) REFERENCES community_profiles (id) ON DELETE CASCADE
);
CREATE INDEX ix_posts_community_created ON posts (community_id, created_at);
CREATE INDEX ix_posts_profile_created ON posts (profile_id, created_at);

CREATE TABLE post_tags (
    post_id BIGINT      NOT NULL,
    tag     VARCHAR(30) NOT NULL,
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE
);
CREATE INDEX ix_post_tags_tag ON post_tags (tag);

CREATE TABLE post_comments (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    post_id    BIGINT        NOT NULL,
    profile_id BIGINT        NOT NULL,
    content    VARCHAR(2000) NOT NULL,
    created_at DATETIME(6)   NOT NULL,
    updated_at DATETIME(6)   NOT NULL,
    CONSTRAINT pk_post_comments PRIMARY KEY (id),
    CONSTRAINT fk_post_comments_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_comments_profile FOREIGN KEY (profile_id) REFERENCES community_profiles (id) ON DELETE CASCADE
);

CREATE TABLE post_likes (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    post_id    BIGINT      NOT NULL,
    profile_id BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_post_likes PRIMARY KEY (id),
    CONSTRAINT uk_post_likes UNIQUE (post_id, profile_id),
    CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_likes_profile FOREIGN KEY (profile_id) REFERENCES community_profiles (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------- Comentarios en perfiles
CREATE TABLE profile_comments (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    author_profile_id BIGINT        NOT NULL,
    target_profile_id BIGINT        NOT NULL,
    content           VARCHAR(2000) NOT NULL,
    created_at        DATETIME(6)   NOT NULL,
    updated_at        DATETIME(6)   NOT NULL,
    CONSTRAINT pk_profile_comments PRIMARY KEY (id),
    CONSTRAINT fk_profile_comments_author FOREIGN KEY (author_profile_id) REFERENCES community_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_profile_comments_target FOREIGN KEY (target_profile_id) REFERENCES community_profiles (id) ON DELETE CASCADE
);
CREATE INDEX ix_profile_comments_target ON profile_comments (target_profile_id, created_at);

-- ---------------------------------------------------------------- Notificaciones
CREATE TABLE notifications (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    receiver_profile_id BIGINT       NOT NULL,
    sender_profile_id   BIGINT,
    type                VARCHAR(30)  NOT NULL,
    message             VARCHAR(255) NOT NULL,
    is_read             BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          DATETIME(6)  NOT NULL,
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_receiver FOREIGN KEY (receiver_profile_id) REFERENCES community_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_sender FOREIGN KEY (sender_profile_id) REFERENCES community_profiles (id) ON DELETE SET NULL
);
CREATE INDEX ix_notifications_receiver ON notifications (receiver_profile_id, is_read, created_at);

-- ---------------------------------------------------------------- Chat
CREATE TABLE chat_messages (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    community_id BIGINT        NOT NULL,
    profile_id   BIGINT        NOT NULL,
    content      VARCHAR(2000) NOT NULL,
    sent_at      DATETIME(6)   NOT NULL,
    CONSTRAINT pk_chat_messages PRIMARY KEY (id),
    CONSTRAINT fk_chat_community FOREIGN KEY (community_id) REFERENCES communities (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_profile FOREIGN KEY (profile_id) REFERENCES community_profiles (id) ON DELETE CASCADE
);
CREATE INDEX ix_chat_community_sent ON chat_messages (community_id, sent_at);
