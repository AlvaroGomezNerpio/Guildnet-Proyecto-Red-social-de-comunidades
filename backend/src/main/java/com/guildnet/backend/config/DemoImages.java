package com.guildnet.backend.config;

import com.guildnet.backend.storage.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Imágenes para los datos de ejemplo, tomadas de una carpeta local
 * ({@code guildnet.demo-images.dir}). Cada imagen se recorta al formato que se
 * necesita (avatar cuadrado o banner panorámico) y se guarda reducida en
 * uploads/, así la página no carga fondos de pantalla de varios megas.
 * <p>
 * Las imágenes no forman parte del repositorio: si la carpeta no existe, los
 * datos de ejemplo se crean sin imágenes.
 */
@Slf4j
@Component
public class DemoImages {

    private static final int AVATAR_SIZE = 400;
    private static final int BANNER_WIDTH = 1600;
    private static final int BANNER_HEIGHT = 500;
    /** Una imagen se considera panorámica (apta para banner) a partir de esta proporción. */
    private static final double WIDE_RATIO = 1.3;
    private static final List<String> EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".gif");

    private final Path dir;
    private final FileStorageService storage;

    private List<Path> files;
    private int cursor;

    public DemoImages(@Value("${guildnet.demo-images.dir:}") String dir, FileStorageService storage) {
        this.dir = dir == null || dir.isBlank() ? null : Path.of(dir);
        this.storage = storage;
    }

    public boolean available() {
        return !files().isEmpty();
    }

    /** Guarda un avatar cuadrado y devuelve su URL. */
    public Optional<String> avatar(FileStorageService.Folder folder) {
        return next(false).map(image -> store(crop(image, AVATAR_SIZE, AVATAR_SIZE), folder));
    }

    /** Guarda un banner panorámico y devuelve su URL. */
    public Optional<String> banner(FileStorageService.Folder folder) {
        return next(true).map(image -> store(crop(image, BANNER_WIDTH, BANNER_HEIGHT), folder));
    }

    // ------------------------------------------------------------------ Selección

    private List<Path> files() {
        if (files == null) {
            files = listFiles();
            if (dir != null) {
                log.info("Imágenes de ejemplo: {} archivos en {}", files.size(), dir);
            }
        }
        return files;
    }

    private List<Path> listFiles() {
        if (dir == null || !Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(Files::isRegularFile)
                    .filter(p -> EXTENSIONS.stream().anyMatch(p.getFileName().toString().toLowerCase(Locale.ROOT)::endsWith))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            log.warn("No se pudo leer la carpeta de imágenes de ejemplo {}", dir, e);
            return List.of();
        }
    }

    /**
     * Siguiente imagen con la orientación pedida. Recorre la carpeta a saltos
     * (no en orden) para que las imágenes elegidas sean variadas; el orden es
     * siempre el mismo, así los datos de ejemplo salen iguales en cada equipo.
     */
    private Optional<BufferedImage> next(boolean wide) {
        List<Path> all = files();
        int n = all.size();
        if (n == 0) {
            return Optional.empty();
        }
        int stride = coprimeStride(n);
        for (int tries = 0; tries < n; tries++) {
            Path candidate = all.get((int) ((long) cursor * stride % n));
            cursor = (cursor + 1) % n;
            Optional<double[]> size = dimensions(candidate);
            if (size.isEmpty()) continue;
            boolean isWide = size.get()[0] / size.get()[1] >= WIDE_RATIO;
            if (isWide != wide) continue;
            Optional<BufferedImage> image = read(candidate);
            if (image.isPresent()) return image;
        }
        return Optional.empty();
    }

    /** Salto que recorre todas las posiciones antes de repetir (primo con n). */
    private static int coprimeStride(int n) {
        int stride = Math.max(1, (int) (n * 0.618));
        while (gcd(stride, n) != 1) stride++;
        return stride;
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    /** Ancho y alto leyendo solo la cabecera del archivo. */
    private static Optional<double[]> dimensions(Path file) {
        try (ImageInputStream in = ImageIO.createImageInputStream(file.toFile())) {
            if (in == null) return Optional.empty();
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) return Optional.empty();
            ImageReader reader = readers.next();
            try {
                reader.setInput(in);
                return Optional.of(new double[]{reader.getWidth(0), reader.getHeight(0)});
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException e) {
            return Optional.empty();
        }
    }

    private static Optional<BufferedImage> read(Path file) {
        try {
            return Optional.ofNullable(ImageIO.read(file.toFile()));
        } catch (IOException | RuntimeException e) {
            log.debug("Imagen de ejemplo no válida: {}", file, e);
            return Optional.empty();
        }
    }

    // ------------------------------------------------------------------ Procesado

    /** Recorta por el centro a la proporción pedida y escala al tamaño final. */
    private static BufferedImage crop(BufferedImage source, int width, int height) {
        double target = (double) width / height;
        int cropW = source.getWidth();
        int cropH = source.getHeight();
        if ((double) cropW / cropH > target) {
            cropW = (int) Math.round(cropH * target);
        } else {
            cropH = (int) Math.round(cropW / target);
        }
        int x = (source.getWidth() - cropW) / 2;
        // En retratos, mejor el tercio superior: suele estar el motivo principal
        int y = source.getHeight() > cropH ? (source.getHeight() - cropH) / 3 : 0;

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setColor(new Color(0x151829));
            g.fillRect(0, 0, width, height);
            g.drawImage(source, 0, 0, width, height, x, y, x + cropW, y + cropH, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    private String store(BufferedImage image, FileStorageService.Folder folder) {
        return storage.storeImage(toJpeg(image), folder);
    }

    private static byte[] toJpeg(BufferedImage image) {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ImageOutputStream out = ImageIO.createImageOutputStream(bytes)) {
            writer.setOutput(out);
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(0.85f);
            writer.write(null, new IIOImage(image, null, null), params);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar la imagen de ejemplo", e);
        } finally {
            writer.dispose();
        }
        return bytes.toByteArray();
    }
}
