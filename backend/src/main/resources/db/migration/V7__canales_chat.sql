-- Canales de chat: cada comunidad crea los que quiera (no hay canales por defecto).
CREATE TABLE chat_channels (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    community_id BIGINT       NOT NULL,
    name         VARCHAR(40)  NOT NULL,
    description  VARCHAR(200) NULL,
    created_at   DATETIME(6)  NOT NULL,
    CONSTRAINT pk_chat_channels PRIMARY KEY (id),
    CONSTRAINT uk_chat_channels_name UNIQUE (community_id, name),
    CONSTRAINT fk_chat_channels_community FOREIGN KEY (community_id) REFERENCES communities (id) ON DELETE CASCADE
);

-- Para no perder el historial, las comunidades que ya tenían mensajes los conservan en un canal "general".
INSERT INTO chat_channels (community_id, name, description, created_at)
SELECT DISTINCT m.community_id, 'general', 'Mensajes anteriores a los canales', CURRENT_TIMESTAMP
FROM chat_messages m;

ALTER TABLE chat_messages ADD COLUMN channel_id BIGINT NULL;
UPDATE chat_messages SET channel_id = (
    SELECT c.id FROM chat_channels c WHERE c.community_id = chat_messages.community_id AND c.name = 'general'
);
ALTER TABLE chat_messages MODIFY COLUMN channel_id BIGINT NOT NULL;
ALTER TABLE chat_messages ADD CONSTRAINT fk_chat_messages_channel
    FOREIGN KEY (channel_id) REFERENCES chat_channels (id) ON DELETE CASCADE;
CREATE INDEX ix_chat_messages_channel ON chat_messages (channel_id, sent_at);

-- Nuevo permiso para crear, editar y borrar canales; los roles "Líder" existentes lo reciben.
INSERT INTO permissions (name, description) VALUES ('MANAGE_CHANNELS', 'Crear y gestionar los canales de chat');
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.name = 'MANAGE_CHANNELS' WHERE r.name = 'Líder';
