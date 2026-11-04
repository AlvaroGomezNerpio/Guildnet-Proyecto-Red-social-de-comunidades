package com.guildnet.backend.storage;

import com.guildnet.backend.common.exception.BadRequestException;
import com.guildnet.backend.config.GuildnetProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * Guarda las imágenes subidas en disco local (carpeta configurable en {@code guildnet.uploads.dir})
 * y devuelve la URL pública con la que se sirven bajo {@code /uploads/**}.
 */
@Slf4j
@Service
public class FileStorageService {

    /** Subcarpetas en las que se organizan las imágenes. */
    public enum Folder {
        USERS("users"),
        COMMUNITIES("communities"),
        COMMUNITY_PROFILES("community-profiles");

        private final String path;

        Folder(String path) {
            this.path = path;
        }
    }

    private final Path rootDir;
    private final String publicBaseUrl;

    public FileStorageService(GuildnetProperties properties) {
        this.rootDir = Path.of(properties.uploads().dir()).toAbsolutePath().normalize();
        this.publicBaseUrl = stripTrailingSlash(properties.publicUrl()) + "/uploads/";
    }

    public Path getRootDir() {
        return rootDir;
    }

    /**
     * Guarda una imagen subida por el usuario. Solo se aceptan PNG, JPEG, GIF y WEBP,
     * comprobando la firma real del archivo (no solo la cabecera Content-Type).
     *
     * @return la URL pública de la imagen, o {@code null} si no se ha enviado ningún archivo.
     */
    public String storeImage(MultipartFile file, Folder folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        ImageType type;
        try (InputStream in = file.getInputStream()) {
            type = ImageType.detect(in.readNBytes(12))
                    .orElseThrow(() -> new BadRequestException("Formato de imagen no soportado (usa PNG, JPG, GIF o WEBP)"));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la imagen", e);
        }

        String filename = UUID.randomUUID() + "." + type.extension;
        Path target = resolveFolder(folder).resolve(filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen", e);
        }
        return publicBaseUrl + folder.path + "/" + filename;
    }

    /**
     * Guarda una imagen generada por el propio servidor (p. ej. los datos de ejemplo),
     * con la misma comprobación de formato que las subidas.
     *
     * @return la URL pública de la imagen.
     */
    public String storeImage(byte[] data, Folder folder) {
        ImageType type = ImageType.detect(Arrays.copyOf(data, Math.min(data.length, 12)))
                .orElseThrow(() -> new BadRequestException("Formato de imagen no soportado (usa PNG, JPG, GIF o WEBP)"));
        String filename = UUID.randomUUID() + "." + type.extension;
        try {
            Files.write(resolveFolder(folder).resolve(filename), data);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen", e);
        }
        return publicBaseUrl + folder.path + "/" + filename;
    }

    /**
     * Copia una imagen ya almacenada (identificada por su URL pública) a otra carpeta.
     *
     * @return la URL de la copia, o {@code null} si la URL no corresponde a un archivo existente.
     */
    public String copyImage(String sourceUrl, Folder folder) {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            return null;
        }
        Optional<Path> source = resolveUrl(sourceUrl);
        if (source.isEmpty() || !Files.isRegularFile(source.get())) {
            log.warn("No se ha podido copiar la imagen {}: no existe en disco", sourceUrl);
            return null;
        }

        String name = source.get().getFileName().toString();
        String extension = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "png";
        String filename = UUID.randomUUID() + "." + extension;
        try {
            Files.copy(source.get(), resolveFolder(folder).resolve(filename));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo copiar la imagen", e);
        }
        return publicBaseUrl + folder.path + "/" + filename;
    }

    /** Convierte una URL pública de {@code /uploads/...} en la ruta del archivo en disco. */
    private Optional<Path> resolveUrl(String url) {
        if (url == null) {
            return Optional.empty();
        }
        int idx = url.indexOf("/uploads/");
        if (idx < 0) {
            return Optional.empty();
        }
        Path path = rootDir.resolve(url.substring(idx + "/uploads/".length())).normalize();
        // Evita salir de la carpeta de uploads con rutas del tipo "../../"
        return path.startsWith(rootDir) ? Optional.of(path) : Optional.empty();
    }

    private Path resolveFolder(Folder folder) {
        Path dir = rootDir.resolve(folder.path);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear la carpeta " + dir, e);
        }
        return dir;
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private enum ImageType {
        PNG("png", new byte[]{(byte) 0x89, 'P', 'N', 'G'}),
        JPEG("jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
        GIF("gif", new byte[]{'G', 'I', 'F', '8'}),
        WEBP("webp", null);

        private final String extension;
        private final byte[] magic;

        ImageType(String extension, byte[] magic) {
            this.extension = extension;
            this.magic = magic;
        }

        static Optional<ImageType> detect(byte[] header) {
            for (ImageType type : values()) {
                if (type == WEBP) {
                    if (header.length >= 12
                            && new String(header, 0, 4).equals("RIFF")
                            && new String(header, 8, 4).equals("WEBP")) {
                        return Optional.of(WEBP);
                    }
                } else if (header.length >= type.magic.length
                        && Arrays.equals(Arrays.copyOf(header, type.magic.length), type.magic)) {
                    return Optional.of(type);
                }
            }
            return Optional.empty();
        }
    }
}
