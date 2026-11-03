package com.guildnet.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Configuración propia de la aplicación (prefijo {@code guildnet.*} en application.properties).
 *
 * @param publicUrl URL pública del backend, usada para construir las URLs de las imágenes subidas.
 * @param jwt       configuración de los tokens JWT.
 * @param cors      orígenes permitidos para el frontend.
 * @param uploads   configuración de almacenamiento de archivos.
 */
@ConfigurationProperties(prefix = "guildnet")
public record GuildnetProperties(
        String publicUrl,
        Jwt jwt,
        Cors cors,
        Uploads uploads
) {

    public record Jwt(String secret, Duration expiration) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Uploads(String dir) {
    }
}
