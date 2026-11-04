package com.guildnet.backend.config;

import com.guildnet.backend.auth.AuthService;
import com.guildnet.backend.auth.RegisterRequest;
import com.guildnet.backend.features.category.PostCategoryDTO;
import com.guildnet.backend.features.category.PostCategoryService;
import com.guildnet.backend.features.chat.ChatService;
import com.guildnet.backend.features.chat.dto.ChatChannelDTO;
import com.guildnet.backend.features.community.Community;
import com.guildnet.backend.features.community.CommunityRepository;
import com.guildnet.backend.features.community.CommunityService;
import com.guildnet.backend.features.community.dto.CommunityLinkDTO;
import com.guildnet.backend.features.community.dto.CreateCommunityRequest;
import com.guildnet.backend.features.like.LikeService;
import com.guildnet.backend.features.permission.PermissionRepository;
import com.guildnet.backend.features.permission.PermissionType;
import com.guildnet.backend.features.post.PostService;
import com.guildnet.backend.features.post.dto.PostCreateDTO;
import com.guildnet.backend.features.postcomment.PostCommentService;
import com.guildnet.backend.features.postcomment.dto.PostCommentCreateDTO;
import com.guildnet.backend.features.profile.CommunityProfile;
import com.guildnet.backend.features.profile.CommunityProfileRepository;
import com.guildnet.backend.features.profile.CommunityProfileService;
import com.guildnet.backend.features.profile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.profilecomment.ProfileCommentService;
import com.guildnet.backend.features.profilecomment.dto.ProfileCommentCreateUpdateDTO;
import com.guildnet.backend.features.role.RoleService;
import com.guildnet.backend.features.role.dto.RoleDTO;
import com.guildnet.backend.features.title.TitleService;
import com.guildnet.backend.features.title.TitleMetric;
import com.guildnet.backend.features.title.dto.CreateTitleRequest;
import com.guildnet.backend.features.title.dto.TitleRuleRequest;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.features.user.UserRepository;
import com.guildnet.backend.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Rellena una base de datos vacía con datos de ejemplo para poder probar la aplicación
 * nada más arrancarla. Se desactiva con {@code guildnet.demo-data=false}.
 * <p>
 * Usuarios de ejemplo (contraseña {@value #PASSWORD}): ana@guildnet.local, bruno@guildnet.local
 * y carla@guildnet.local.
 * <p>
 * Si existe la carpeta de imágenes de ejemplo ({@code guildnet.demo-images.dir}),
 * además rellena en cada arranque las imágenes que falten (avatares de usuarios,
 * iconos y banners de comunidades, fotos de perfil), también en bases de datos
 * que ya tenían datos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "guildnet.demo-data", havingValue = "true")
public class DemoDataLoader implements ApplicationRunner {

    static final String PASSWORD = "guildnet123";

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final AuthService authService;
    private final CommunityService communityService;
    private final CommunityProfileService profileService;
    private final PostService postService;
    private final PostCommentService commentService;
    private final ProfileCommentService profileCommentService;
    private final LikeService likeService;
    private final RoleService roleService;
    private final TitleService titleService;
    private final ChatService chatService;
    private final CommunityRepository communityRepository;
    private final CommunityProfileRepository communityProfileRepository;
    private final FileStorageService fileStorageService;
    private final DemoImages demoImages;
    private final PostCategoryService categoryService;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() == 0) {
            createDemoData();
        }
        transactionTemplate.executeWithoutResult(status -> fillMissingImages());
    }

    private void createDemoData() {
        log.info("Base de datos vacía: creando datos de ejemplo (contraseña de los usuarios: {})", PASSWORD);

        User ana = user("Ana", "ana@guildnet.local");
        User bruno = user("Bruno", "bruno@guildnet.local");
        User carla = user("Carla", "carla@guildnet.local");

        // ------------------------------------------------------------ Comunidades
        long boardGames = communityService.create(new CreateCommunityRequest(
                "Juegos de mesa",
                "<p>Un lugar para hablar de <strong>juegos de mesa</strong>: novedades, reseñas y quedadas.</p>",
                "<ol><li>Respeta a los demás.</li><li>Marca los spoilers de campañas.</li><li>Nada de spam.</li></ol>",
                List.of("juegos", "mesa", "ocio"), "#ea580c", "#db2777",
                "rounded", "Jugadores", "¡Bienvenido a la mesa! Preséntate en el chat y cuéntanos a qué te gusta jugar.", false,
                List.of(new CommunityLinkDTO("BoardGameGeek", "https://boardgamegeek.com"))), null, null, ana).id();

        long photography = communityService.create(new CreateCommunityRequest(
                "Fotografía",
                "<p>Comparte tus fotos, pide consejo sobre equipo y aprende técnicas nuevas.</p>",
                "<p>Indica siempre si la foto es tuya.</p>",
                List.of("fotografía", "arte", "cámaras"), "#0284c7", "#22d3ee",
                "serif", "Fotógrafos", "¡Hola! Antes de publicar, recuerda indicar si la foto es tuya.", true,
                List.of()), null, null, bruno).id();

        long programming = communityService.create(new CreateCommunityRequest(
                "Programación",
                "<p>Dudas, proyectos y recursos para aprender a programar.</p>",
                "<p>Comparte el código como bloque de código, no como captura.</p>",
                List.of("programación", "tecnología", "java", "angular"), "#059669", "#14b8a6",
                "pixel", "Programadores", null, false,
                List.of(new CommunityLinkDTO("GitHub", "https://github.com"),
                        new CommunityLinkDTO("Documentación de Angular", "https://angular.dev"))), null, null, carla).id();

        // ------------------------------------------------------------ Miembros
        CommunityProfileDTO brunoInGames = profileService.join(bruno, boardGames, null, true);
        CommunityProfileDTO carlaInGames = profileService.join(carla, boardGames, null, true);
        CommunityProfileDTO anaInPhoto = profileService.join(ana, photography, null, true);
        profileService.join(ana, programming, null, true);
        CommunityProfileDTO brunoInCode = profileService.join(bruno, programming, null, true);

        // ------------------------------------------------------------ Roles y títulos
        RoleDTO moderator = roleService.create(
                new RoleDTO(null, "Moderador", "#ffffff", "#6f42c1", boardGames), ana);
        for (PermissionType type : List.of(PermissionType.EDIT_COMMENTS, PermissionType.DELETE_COMMENTS,
                PermissionType.DELETE_POST)) {
            permissionRepository.findByName(type).ifPresent(p -> roleService.addPermission(moderator.id(), p.getId(), ana));
        }
        profileService.assignRole(brunoInGames.getId(), moderator.id(), ana);

        var veteran = titleService.create(new CreateTitleRequest("Veterano", "#212529", "#ffc107", boardGames), ana);
        profileService.assignTitle(brunoInGames.getId(), veteran.getId(), ana);

        // Títulos automáticos: se consiguen solos al llegar a la actividad indicada
        autoTitle(boardGames, "Primera partida", "#16a34a", TitleMetric.POSTS, 1, ana);
        autoTitle(boardGames, "Popular", "#db2777", TitleMetric.LIKES_RECEIVED, 2, ana);
        autoTitle(programming, "Colaborador", "#0891b2", TitleMetric.COMMENTS, 1, carla);

        // ------------------------------------------------------------ Categorías de publicaciones
        long question = category(boardGames, "Pregunta", "#2563eb", ana);
        long event = category(boardGames, "Evento", "#f59e0b", ana);
        category(boardGames, "Reseña", "#059669", ana);
        long tip = category(photography, "Consejo", "#0284c7", bruno);
        category(photography, "Mi foto", "#db2777", bruno);
        long resource = category(programming, "Recurso", "#059669", carla);
        category(programming, "Duda", "#7c3aed", carla);

        // ------------------------------------------------------------ Publicaciones, comentarios y likes
        long post1 = postService.create(new PostCreateDTO(
                "¿Qué juego recomendáis para empezar?",
                "<p>Quiero introducir a mis amigos en los juegos de mesa modernos. "
                        + "¿Algo sencillo de explicar pero con decisiones interesantes?</p>",
                List.of("recomendaciones", "principiantes"), null, boardGames, question), ana).getId();
        commentService.create(new PostCommentCreateDTO(post1, null,
                "Cualquier juego de colocación de losetas es perfecto para empezar."), bruno);
        commentService.create(new PostCommentCreateDTO(post1, null,
                "Yo empezaría por uno cooperativo, así nadie se siente perdido."), carla);
        likeService.like(post1, brunoInGames.getId(), bruno);
        likeService.like(post1, carlaInGames.getId(), carla);

        long post2 = postService.create(new PostCreateDTO(
                "Quedada el sábado",
                "<p>Este sábado a las <strong>17:00</strong> en la ludoteca del barrio. ¡Traed vuestros juegos!</p>",
                List.of("quedada"), null, boardGames, event), bruno).getId();
        likeService.like(post2, carlaInGames.getId(), carla);

        long post3 = postService.create(new PostCreateDTO(
                "Consejos para fotografía nocturna",
                "<p>Trípode, ISO bajo y exposiciones largas. ¿Qué más añadiríais?</p>",
                List.of("consejos", "noche"), null, photography, tip), bruno).getId();
        commentService.create(new PostCommentCreateDTO(post3, null,
                "Disparar en RAW para poder recuperar sombras después."), ana);
        likeService.like(post3, anaInPhoto.getId(), ana);

        long post4 = postService.create(new PostCreateDTO(
                "Recursos para aprender Angular",
                "<p>Estoy empezando con Angular. La documentación oficial y su tutorial me están sirviendo mucho.</p>",
                List.of("angular", "recursos"), null, programming, resource), bruno).getId();
        commentService.create(new PostCommentCreateDTO(post4, null,
                "Mira también las señales (signals), simplifican mucho el estado."), carla);
        likeService.like(post4, brunoInCode.getId(), bruno);

        // ------------------------------------------------------------ Comentarios de perfil y chat
        profileCommentService.create(carlaInGames.getId(), brunoInGames.getId(),
                new ProfileCommentCreateUpdateDTO("¡Gracias por organizar la última partida!"), carla);

        long general = chatService.createChannel(boardGames, new ChatChannelDTO(null, "general", "Charla sobre juegos de mesa", null), ana).id();
        long meetups = chatService.createChannel(boardGames, new ChatChannelDTO(null, "quedadas", "Organiza partidas en persona", null), ana).id();
        chatService.createChannel(programming, new ChatChannelDTO(null, "ayuda", "Pregunta tus dudas de código", null), carla);
        chatService.send(general, "¡Hola a todos! ¿Quién se apunta el sábado?", bruno);
        chatService.send(general, "¡Yo voy! Llevo un par de juegos nuevos.", carla);
        chatService.send(general, "Genial, nos vemos allí 🙌", ana);
        chatService.send(meetups, "¿Alguien se apunta el domingo por la tarde?", ana);

        log.info("Datos de ejemplo creados: 3 usuarios, 3 comunidades y 4 publicaciones");
    }

    /** Pone una imagen de ejemplo en todo lo que no tenga, para que la página no se vea vacía. */
    private void fillMissingImages() {
        if (!demoImages.available()) {
            return;
        }
        int filled = 0;
        for (User user : userRepository.findAll()) {
            if (user.getProfileImage() == null) {
                Optional<String> url = demoImages.avatar(FileStorageService.Folder.USERS);
                if (url.isPresent()) {
                    user.setProfileImage(url.get());
                    userRepository.save(user);
                    filled++;
                }
            }
        }
        for (Community community : communityRepository.findAll()) {
            boolean changed = false;
            if (community.getImage() == null) {
                Optional<String> url = demoImages.avatar(FileStorageService.Folder.COMMUNITIES);
                if (url.isPresent()) {
                    community.setImage(url.get());
                    changed = true;
                }
            }
            if (community.getBanner() == null) {
                Optional<String> url = demoImages.banner(FileStorageService.Folder.COMMUNITIES);
                if (url.isPresent()) {
                    community.setBanner(url.get());
                    changed = true;
                }
            }
            if (changed) {
                communityRepository.save(community);
                filled++;
            }
        }
        // Los perfiles de comunidad usan la misma imagen que su usuario, como al unirse normalmente
        for (CommunityProfile profile : communityProfileRepository.findAll()) {
            if (profile.getProfileImage() == null && profile.getUser().getProfileImage() != null) {
                profile.setProfileImage(fileStorageService.copyImage(
                        profile.getUser().getProfileImage(), FileStorageService.Folder.COMMUNITY_PROFILES));
                communityProfileRepository.save(profile);
                filled++;
            }
        }
        if (filled > 0) {
            log.info("Imágenes de ejemplo: completados {} elementos sin imagen", filled);
        }
    }

    private void autoTitle(long communityId, String name, String background, TitleMetric metric, int threshold, User owner) {
        var title = titleService.create(new CreateTitleRequest(name, null, background, communityId), owner);
        titleService.setRule(title.getId(), new TitleRuleRequest(metric, threshold), owner);
    }

    private long category(long communityId, String name, String background, User owner) {
        return categoryService.create(communityId, new PostCategoryDTO(null, name, null, background, null), owner).id();
    }

    private User user(String name, String email) {
        authService.register(new RegisterRequest(name, email, PASSWORD), null);
        return userRepository.findByEmail(email).orElseThrow();
    }
}
