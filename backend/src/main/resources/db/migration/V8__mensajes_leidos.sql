-- Último mensaje leído por cada miembro en cada canal, para mostrar cuántos mensajes nuevos tiene.
CREATE TABLE chat_channel_reads (
    profile_id           BIGINT NOT NULL,
    channel_id           BIGINT NOT NULL,
    last_read_message_id BIGINT NOT NULL,
    CONSTRAINT pk_chat_channel_reads PRIMARY KEY (profile_id, channel_id),
    CONSTRAINT fk_chat_reads_profile FOREIGN KEY (profile_id) REFERENCES community_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_reads_channel FOREIGN KEY (channel_id) REFERENCES chat_channels (id) ON DELETE CASCADE
);
