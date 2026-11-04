package com.guildnet.backend.config;

import com.guildnet.backend.auth.AuthService;
import com.guildnet.backend.auth.RegisterRequest;
import com.guildnet.backend.features.chat.ChatService;
import com.guildnet.backend.features.community.CommunityService;
import com.guildnet.backend.features.community.dto.CreateCommunityRequest;
import com.guildnet.backend.features.like.LikeService;
import com.guildnet.backend.features.permission.PermissionRepository;
import com.guildnet.backend.features.permission.PermissionType;
import com.guildnet.backend.features.post.PostService;
import com.guildnet.backend.features.post.dto.PostCreateDTO;
import com.guildnet.backend.features.postcomment.PostCommentService;
import com.guildnet.backend.features.postcomment.dto.PostCommentCreateDTO;
import com.guildnet.backend.features.profile.CommunityProfileService;
import com.guildnet.backend.features.profile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.profilecomment.ProfileCommentService;
import com.guildnet.backend.features.profilecomment.dto.ProfileCommentCreateUpdateDTO;
import com.guildnet.backend.features.role.RoleService;
import com.guildnet.backend.features.role.dto.RoleDTO;
import com.guildnet.backend.features.title.TitleService;
import com.guildnet.backend.features.title.dto.CreateTitleRequest;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.features.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Rellena una base de datos vacía con datos de ejemplo para poder probar la aplicación
 * nada más arrancarla. Se desactiva con {@code guildnet.demo-data=false}.
 * <p>
 * Usuarios de ejemplo (contraseña {@value #PASSWORD}): ana@guildnet.local, bruno@guildnet.local
 * y carla@guildnet.local.
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

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        log.info("Base de datos vacía: creando datos de ejemplo (contraseña de los usuarios: {})", PASSWORD);

        User ana = user("Ana", "ana@guildnet.local");
        User bruno = user("Bruno", "bruno@guildnet.local");
        User carla = user("Carla", "carla@guildnet.local");

        // ------------------------------------------------------------ Comunidades
        long boardGames = communityService.create(new CreateCommunityRequest(
                "Juegos de mesa",
                "<p>Un lugar para hablar de <strong>juegos de mesa</strong>: novedades, reseñas y quedadas.</p>",
                "<ol><li>Respeta a los demás.</li><li>Marca los spoilers de campañas.</li><li>Nada de spam.</li></ol>",
                List.of("juegos", "mesa", "ocio")), null, null, ana).id();

        long photography = communityService.create(new CreateCommunityRequest(
                "Fotografía",
                "<p>Comparte tus fotos, pide consejo sobre equipo y aprende técnicas nuevas.</p>",
                "<p>Indica siempre si la foto es tuya.</p>",
                List.of("fotografía", "arte", "cámaras")), null, null, bruno).id();

        long programming = communityService.create(new CreateCommunityRequest(
                "Programación",
                "<p>Dudas, proyectos y recursos para aprender a programar.</p>",
                "<p>Comparte el código como bloque de código, no como captura.</p>",
                List.of("programación", "tecnología", "java", "angular")), null, null, carla).id();

        // ------------------------------------------------------------ Miembros
        CommunityProfileDTO brunoInGames = profileService.join(bruno, boardGames, null);
        CommunityProfileDTO carlaInGames = profileService.join(carla, boardGames, null);
        CommunityProfileDTO anaInPhoto = profileService.join(ana, photography, null);
        profileService.join(ana, programming, null);
        CommunityProfileDTO brunoInCode = profileService.join(bruno, programming, null);

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

        // ------------------------------------------------------------ Publicaciones, comentarios y likes
        long post1 = postService.create(new PostCreateDTO(
                "¿Qué juego recomendáis para empezar?",
                "<p>Quiero introducir a mis amigos en los juegos de mesa modernos. "
                        + "¿Algo sencillo de explicar pero con decisiones interesantes?</p>",
                List.of("recomendaciones", "principiantes"), null, boardGames), ana).getId();
        commentService.create(new PostCommentCreateDTO(post1, null,
                "Cualquier juego de colocación de losetas es perfecto para empezar."), bruno);
        commentService.create(new PostCommentCreateDTO(post1, null,
                "Yo empezaría por uno cooperativo, así nadie se siente perdido."), carla);
        likeService.like(post1, brunoInGames.getId(), bruno);
        likeService.like(post1, carlaInGames.getId(), carla);

        long post2 = postService.create(new PostCreateDTO(
                "Quedada el sábado",
                "<p>Este sábado a las <strong>17:00</strong> en la ludoteca del barrio. ¡Traed vuestros juegos!</p>",
                List.of("quedada"), null, boardGames), bruno).getId();
        likeService.like(post2, carlaInGames.getId(), carla);

        long post3 = postService.create(new PostCreateDTO(
                "Consejos para fotografía nocturna",
                "<p>Trípode, ISO bajo y exposiciones largas. ¿Qué más añadiríais?</p>",
                List.of("consejos", "noche"), null, photography), bruno).getId();
        commentService.create(new PostCommentCreateDTO(post3, null,
                "Disparar en RAW para poder recuperar sombras después."), ana);
        likeService.like(post3, anaInPhoto.getId(), ana);

        long post4 = postService.create(new PostCreateDTO(
                "Recursos para aprender Angular",
                "<p>Estoy empezando con Angular. La documentación oficial y su tutorial me están sirviendo mucho.</p>",
                List.of("angular", "recursos"), null, programming), bruno).getId();
        commentService.create(new PostCommentCreateDTO(post4, null,
                "Mira también las señales (signals), simplifican mucho el estado."), carla);
        likeService.like(post4, brunoInCode.getId(), bruno);

        // ------------------------------------------------------------ Comentarios de perfil y chat
        profileCommentService.create(carlaInGames.getId(), brunoInGames.getId(),
                new ProfileCommentCreateUpdateDTO("¡Gracias por organizar la última partida!"), carla);
        chatService.send(boardGames, "¡Hola a todos! ¿Quién se apunta el sábado?", bruno);
        chatService.send(boardGames, "¡Yo voy! Llevo un par de juegos nuevos.", carla);
        chatService.send(boardGames, "Genial, nos vemos allí 🙌", ana);

        log.info("Datos de ejemplo creados: 3 usuarios, 3 comunidades y 4 publicaciones");
    }

    private User user(String name, String email) {
        authService.register(new RegisterRequest(name, email, PASSWORD), null);
        return userRepository.findByEmail(email).orElseThrow();
    }
}
