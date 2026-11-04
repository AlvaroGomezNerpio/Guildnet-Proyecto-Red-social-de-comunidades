package com.guildnet.backend.config;

import com.guildnet.backend.storage.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DemoImagesTest {

    @TempDir
    Path tmp;

    @Test
    void recortaAvataresCuadradosYBannersPanoramicosDesdeLaCarpeta() throws Exception {
        Path source = Files.createDirectories(tmp.resolve("imagenes"));
        ImageIO.write(new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB), "jpg", source.resolve("fondo.jpg").toFile());
        ImageIO.write(new BufferedImage(800, 1200, BufferedImage.TYPE_INT_ARGB), "png", source.resolve("retrato.png").toFile());
        Files.writeString(source.resolve("notas.txt"), "no es una imagen");

        Path uploads = tmp.resolve("uploads");
        FileStorageService storage = new FileStorageService(new GuildnetProperties(
                "http://localhost:8080", new GuildnetProperties.Jwt("x", Duration.ofHours(1)),
                new GuildnetProperties.Cors(List.of()), new GuildnetProperties.Uploads(uploads.toString())));
        DemoImages images = new DemoImages(source.toString(), storage);

        assertThat(images.available()).isTrue();
        String avatar = images.avatar(FileStorageService.Folder.USERS).orElseThrow();
        String banner = images.banner(FileStorageService.Folder.COMMUNITIES).orElseThrow();

        BufferedImage avatarFile = ImageIO.read(uploads.resolve(avatar.substring(avatar.indexOf("/uploads/") + 9)).toFile());
        BufferedImage bannerFile = ImageIO.read(uploads.resolve(banner.substring(banner.indexOf("/uploads/") + 9)).toFile());
        assertThat(avatarFile.getWidth()).isEqualTo(400);
        assertThat(avatarFile.getHeight()).isEqualTo(400);
        assertThat(bannerFile.getWidth()).isEqualTo(1600);
        assertThat(bannerFile.getHeight()).isEqualTo(500);
    }

    @Test
    void sinCarpetaNoHayImagenes() {
        DemoImages images = new DemoImages(tmp.resolve("no-existe").toString(), null);
        assertThat(images.available()).isFalse();
        assertThat(images.avatar(FileStorageService.Folder.USERS)).isEmpty();
    }
}
