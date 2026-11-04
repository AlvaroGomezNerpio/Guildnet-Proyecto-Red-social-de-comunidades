package com.guildnet.backend;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración de la API sobre H2 en memoria. Comprueban sobre todo
 * las reglas de autorización: cada usuario solo puede actuar sobre lo suyo
 * o sobre aquello para lo que su rol le da permiso.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private com.guildnet.backend.features.chat.ChatService chatService;

    @Autowired
    private com.guildnet.backend.features.user.UserRepository userRepository;

    // ------------------------------------------------------------------ Autenticación

    @Test
    void registerLoginAndMe() throws Exception {
        String email = uniqueEmail();
        register("Ana", email);

        String token = JsonPath.read(mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "$.token");

        mvc.perform(auth(get("/api/v1/user/me"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Ana"))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        String email = uniqueEmail();
        register("Ana", email);

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicatedEmailReturnsConflict() throws Exception {
        String email = uniqueEmail();
        register("Ana", email);
        registerRequest("Otra", email).andExpect(status().isConflict());
    }

    @Test
    void invalidRegistrationReturnsValidationErrors() throws Exception {
        registerRequest("", "no-es-un-email", "corta")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void requestsWithoutValidTokenReturn401() throws Exception {
        mvc.perform(get("/api/v1/user/me")).andExpect(status().isUnauthorized());
        mvc.perform(auth(get("/api/v1/user/me"), "token-falso")).andExpect(status().isUnauthorized());
    }

    @Test
    void publicEndpointsWorkWithoutToken() throws Exception {
        mvc.perform(get("/api/v1/communities/popular")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/communities/search").param("name", "x")).andExpect(status().isOk());
    }

    // ------------------------------------------------------------------ Comunidades

    @Test
    void creatorGetsLeaderRoleAndOnlyAuthorizedUsersCanManageCommunity() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String other = register("Other", uniqueEmail());
        long communityId = createCommunity(owner, "Juegos de mesa");

        // El creador tiene perfil con el rol "Líder"
        mvc.perform(auth(get("/api/v1/profiles/me/community/" + communityId), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol.name").value("Líder"));

        joinCommunity(other, communityId);

        // Un miembro sin permisos no puede editar ni borrar la comunidad
        mvc.perform(auth(multipart("/api/v1/communities/" + communityId)
                        .file(json("data", "{\"name\":\"Hackeada\"}"))
                        .with(r -> { r.setMethod("PUT"); return r; }), other))
                .andExpect(status().isForbidden());
        mvc.perform(auth(delete("/api/v1/communities/" + communityId), other))
                .andExpect(status().isForbidden());

        // El creador sí
        mvc.perform(auth(multipart("/api/v1/communities/" + communityId)
                        .file(json("data", "{\"name\":\"Juegos de rol\",\"tags\":[\"rol\"]}"))
                        .with(r -> { r.setMethod("PUT"); return r; }), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Juegos de rol"));
        mvc.perform(auth(delete("/api/v1/communities/" + communityId), owner))
                .andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/v1/communities/" + communityId), owner))
                .andExpect(status().isNotFound());
    }

    @Test
    void communityPaletteHasDefaultsCanBeChangedAndIsValidated() throws Exception {
        String owner = register("Owner", uniqueEmail());
        long communityId = createCommunity(owner, "Colores " + UUID.randomUUID());

        // Sin colores: se usa la paleta de GuildNet
        mvc.perform(auth(get("/api/v1/communities/" + communityId), owner))
                .andExpect(jsonPath("$.primaryColor").value("#7c3aed"))
                .andExpect(jsonPath("$.accentColor").value("#06b6d4"));

        // Colores exactos elegidos por la comunidad (se guardan en minúsculas)
        mvc.perform(auth(multipart("/api/v1/communities/" + communityId)
                        .file(json("data", "{\"name\":\"Paleta\",\"primaryColor\":\"#EA580C\",\"accentColor\":\"#db2777\"}"))
                        .with(r -> { r.setMethod("PUT"); return r; }), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primaryColor").value("#ea580c"))
                .andExpect(jsonPath("$.accentColor").value("#db2777"));

        // Si no se envían, se conservan
        mvc.perform(auth(multipart("/api/v1/communities/" + communityId)
                        .file(json("data", "{\"name\":\"Paleta\"}"))
                        .with(r -> { r.setMethod("PUT"); return r; }), owner))
                .andExpect(jsonPath("$.primaryColor").value("#ea580c"));

        // Formato no válido (podría usarse para inyectar CSS)
        mvc.perform(auth(multipart("/api/v1/communities/" + communityId)
                        .file(json("data", "{\"name\":\"Paleta\",\"primaryColor\":\"red; background:url(x)\"}"))
                        .with(r -> { r.setMethod("PUT"); return r; }), owner))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rolesAndTitlesDefaultToTheCommunityColour() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String body = mvc.perform(auth(multipart("/api/v1/communities/create")
                        .file(json("community", "{\"name\":\"Naranja " + UUID.randomUUID() + "\",\"primaryColor\":\"#ea580c\"}")), owner))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long communityId = ((Number) JsonPath.read(body, "$.id")).longValue();

        // El rol "Líder" que se crea con la comunidad lleva su color
        mvc.perform(auth(get("/api/v1/profiles/me/community/" + communityId), owner))
                .andExpect(jsonPath("$.rol.backgroundColor").value("#ea580c"));

        // Un rol o un título sin colores toma el de la comunidad, con texto legible encima
        mvc.perform(auth(post("/api/v1/roles").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Moderador\",\"communityId\":" + communityId + "}"), owner))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.backgroundColor").value("#ea580c"))
                .andExpect(jsonPath("$.textColor").value("#111322"));
        mvc.perform(auth(post("/api/v1/titles").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Veterano\",\"backgroundColor\":\"#1E3A8A\",\"communityId\":" + communityId + "}"), owner))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.backgroundColor").value("#1e3a8a"))
                .andExpect(jsonPath("$.textColor").value("#ffffff"));
    }

    @Test
    void communityCustomizationRulesAcceptanceAndWelcomeMessage() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String member = register("Member", uniqueEmail());
        long communityId = createCommunity(owner, "Personalizada " + UUID.randomUUID());

        mvc.perform(auth(multipart("/api/v1/communities/" + communityId)
                        .file(json("data", "{\"name\":\"Entrenadores " + UUID.randomUUID() + "\",\"headingFont\":\"pixel\","
                                + "\"membersLabel\":\"Entrenadores\",\"welcomeMessage\":\"¡Hola, entrenador!\","
                                + "\"requireRulesAcceptance\":true,"
                                + "\"links\":[{\"label\":\"Discord\",\"url\":\"https://discord.gg/abc\"}]}"))
                        .with(r -> { r.setMethod("PUT"); return r; }), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headingFont").value("pixel"))
                .andExpect(jsonPath("$.membersLabel").value("Entrenadores"))
                .andExpect(jsonPath("$.links[0].label").value("Discord"))
                .andExpect(jsonPath("$.memberCount").value(1));

        // Enlaces que no son http(s) se rechazan (evita javascript: y similares)
        mvc.perform(auth(multipart("/api/v1/communities/" + communityId)
                        .file(json("data", "{\"name\":\"X\",\"links\":[{\"label\":\"Mal\",\"url\":\"javascript:alert(1)\"}]}"))
                        .with(r -> { r.setMethod("PUT"); return r; }), owner))
                .andExpect(status().isBadRequest());

        // Sin aceptar las normas no se puede entrar; aceptándolas sí, y llega el mensaje propio
        mvc.perform(auth(post("/api/v1/profiles/auto-create/community/" + communityId), member))
                .andExpect(status().isBadRequest());
        String body = mvc.perform(auth(post("/api/v1/profiles/auto-create/community/" + communityId)
                        .param("acceptRules", "true"), member))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long profileId = ((Number) JsonPath.read(body, "$.id")).longValue();
        mvc.perform(auth(get("/api/v1/notifications/profile/" + profileId), member))
                .andExpect(jsonPath("$[0].message").value("¡Hola, entrenador!"));
    }

    @Test
    void pinnedPostsRequirePermissionAreLimitedAndLeaveTheFeed() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String member = register("Member", uniqueEmail());
        long communityId = createCommunity(owner, "Fijadas " + UUID.randomUUID());
        joinCommunity(member, communityId);
        long[] posts = new long[4];
        for (int i = 0; i < posts.length; i++) {
            posts[i] = createPost(owner, communityId, "Post " + i, "<p>x</p>");
        }

        // Un miembro sin permiso no puede fijar
        mvc.perform(auth(put("/api/v1/posts/" + posts[0] + "/pin"), member)).andExpect(status().isForbidden());

        // El creador fija tres; la cuarta supera el límite
        for (int i = 0; i < 3; i++) {
            mvc.perform(auth(put("/api/v1/posts/" + posts[i] + "/pin"), owner))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pinned").value(true));
        }
        mvc.perform(auth(put("/api/v1/posts/" + posts[3] + "/pin"), owner)).andExpect(status().isConflict());

        // Las fijadas salen aparte y no se repiten en el muro; fijar no la marca como editada
        mvc.perform(auth(get("/api/v1/posts/community/" + communityId + "/pinned"), owner))
                .andExpect(jsonPath("$.length()").value(3));
        mvc.perform(auth(get("/api/v1/posts/community/" + communityId), owner))
                .andExpect(jsonPath("$.totalElements").value(1));
        String detail = mvc.perform(auth(get("/api/v1/posts/" + posts[0]), owner))
                .andReturn().getResponse().getContentAsString();
        String createdAt = JsonPath.read(detail, "$.createdAt");
        String updatedAt = JsonPath.read(detail, "$.updatedAt");
        org.assertj.core.api.Assertions.assertThat(updatedAt.substring(0, 19)).isEqualTo(createdAt.substring(0, 19));

        mvc.perform(auth(delete("/api/v1/posts/" + posts[0] + "/pin"), owner))
                .andExpect(jsonPath("$.pinned").value(false));
    }

    @Test
    void postCategoriesAreManagedFilteredAndScopedToTheirCommunity() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String member = register("Member", uniqueEmail());
        long communityId = createCommunity(owner, "Categorías " + UUID.randomUUID());
        long otherCommunity = createCommunity(owner, "Otra " + UUID.randomUUID());
        joinCommunity(member, communityId);

        // Solo quien gestiona la comunidad crea categorías; sin colores usa el de la comunidad
        mvc.perform(auth(post("/api/v1/communities/" + communityId + "/categories"), member)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Guía\"}"))
                .andExpect(status().isForbidden());
        String body = mvc.perform(auth(post("/api/v1/communities/" + communityId + "/categories"), owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Guía\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.backgroundColor").value("#7c3aed"))
                .andExpect(jsonPath("$.textColor").value("#ffffff"))
                .andReturn().getResponse().getContentAsString();
        long guide = ((Number) JsonPath.read(body, "$.id")).longValue();
        mvc.perform(auth(post("/api/v1/communities/" + communityId + "/categories"), owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"guía\"}"))
                .andExpect(status().isConflict());
        String foreign = mvc.perform(auth(post("/api/v1/communities/" + otherCommunity + "/categories"), owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Ajena\"}"))
                .andReturn().getResponse().getContentAsString();
        long foreignId = ((Number) JsonPath.read(foreign, "$.id")).longValue();

        // Publicar con categoría (una de otra comunidad se rechaza) y filtrar el muro
        String postJson = "{\"title\":\"Cómo empezar\",\"content\":\"<p>x</p>\",\"communityId\":" + communityId
                + ",\"categoryId\":%d}";
        mvc.perform(auth(post("/api/v1/posts"), member).contentType(MediaType.APPLICATION_JSON)
                        .content(postJson.formatted(foreignId)))
                .andExpect(status().isBadRequest());
        String created = mvc.perform(auth(post("/api/v1/posts"), member).contentType(MediaType.APPLICATION_JSON)
                        .content(postJson.formatted(guide)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category.name").value("Guía"))
                .andReturn().getResponse().getContentAsString();
        long postId = ((Number) JsonPath.read(created, "$.id")).longValue();
        createPost(member, communityId, "Sin categoría", "<p>y</p>");

        mvc.perform(auth(get("/api/v1/posts/community/" + communityId).param("category", "" + guide), member))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Cómo empezar"));
        mvc.perform(auth(get("/api/v1/posts/community/" + communityId), member))
                .andExpect(jsonPath("$.totalElements").value(2));

        // Al borrar la categoría la publicación se conserva, sin categoría
        mvc.perform(auth(delete("/api/v1/categories/" + guide), owner)).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/v1/posts/" + postId), member))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").doesNotExist());
    }

    @Test
    void automaticTitlesAreAwardedByActivityAndRetroactively() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String member = register("Member", uniqueEmail());
        long communityId = createCommunity(owner, "Logros " + UUID.randomUUID());
        long memberProfile = joinCommunity(member, communityId);

        long writer = createTitle(owner, communityId, "Escritor");
        mvc.perform(auth(put("/api/v1/titles/" + writer + "/rule"), member)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"metric\":\"POSTS\",\"threshold\":2}"))
                .andExpect(status().isForbidden());
        mvc.perform(auth(put("/api/v1/titles/" + writer + "/rule"), owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"metric\":\"POSTS\",\"threshold\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autoMetric").value("POSTS"))
                .andExpect(jsonPath("$.autoThreshold").value(2));

        // Con una publicación aún no; con la segunda se consigue y llega el aviso
        createPost(member, communityId, "Uno", "<p>1</p>");
        mvc.perform(auth(get("/api/v1/profiles/" + memberProfile), member))
                .andExpect(jsonPath("$.titles.length()").value(0));
        createPost(member, communityId, "Dos", "<p>2</p>");
        mvc.perform(auth(get("/api/v1/profiles/" + memberProfile), member))
                .andExpect(jsonPath("$.titles[0].title").value("Escritor"));
        mvc.perform(auth(get("/api/v1/notifications/profile/" + memberProfile), member))
                .andExpect(jsonPath("$[0].message").value("¡Has conseguido el título \"Escritor\"!"));

        // Una regla nueva se aplica también a quien ya la cumplía
        long prolific = createTitle(owner, communityId, "Prolífico");
        mvc.perform(auth(put("/api/v1/titles/" + prolific + "/rule"), owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"metric\":\"POSTS\",\"threshold\":1}"))
                .andExpect(status().isOk());
        mvc.perform(auth(get("/api/v1/profiles/" + memberProfile), member))
                .andExpect(jsonPath("$.titles.length()").value(2));

        // Quitar la regla no quita el título a quien ya lo tiene
        mvc.perform(auth(delete("/api/v1/titles/" + writer + "/rule"), owner))
                .andExpect(jsonPath("$.autoMetric").doesNotExist());
        mvc.perform(auth(get("/api/v1/profiles/" + memberProfile), member))
                .andExpect(jsonPath("$.titles.length()").value(2));

        // Cantidades fuera de rango se rechazan
        mvc.perform(auth(put("/api/v1/titles/" + writer + "/rule"), owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"metric\":\"POSTS\",\"threshold\":0}"))
                .andExpect(status().isBadRequest());
    }

    private long createTitle(String token, long communityId, String name) throws Exception {
        String body = mvc.perform(auth(post("/api/v1/titles"), token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + name + "\",\"communityId\":" + communityId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    @Test
    void chatChannelsAreCreatedByTheCommunityWithTheirOwnPermission() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String member = register("Member", uniqueEmail());
        String outsider = register("Outsider", uniqueEmail());
        long communityId = createCommunity(owner, "Canales " + UUID.randomUUID());
        joinCommunity(member, communityId);
        String channels = "/api/v1/communities/" + communityId + "/channels";

        // Sin canales por defecto
        mvc.perform(auth(get(channels), member)).andExpect(jsonPath("$.length()").value(0));

        // Crear canales requiere MANAGE_CHANNELS (el creador lo tiene; un miembro sin rol, no)
        mvc.perform(auth(post(channels), member).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"general\"}"))
                .andExpect(status().isForbidden());
        String body = mvc.perform(auth(post(channels), owner).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"eventos\",\"description\":\"Quedadas y partidas\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long channelId = ((Number) JsonPath.read(body, "$.id")).longValue();
        mvc.perform(auth(post(channels), owner).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Eventos\"}"))
                .andExpect(status().isConflict());
        mvc.perform(auth(post(channels), owner).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"<script>\"}"))
                .andExpect(status().isBadRequest());

        // Los miembros ven los canales; quien no es miembro, no
        mvc.perform(auth(get(channels), member))
                .andExpect(jsonPath("$[0].name").value("eventos"))
                .andExpect(jsonPath("$[0].description").value("Quedadas y partidas"));
        mvc.perform(auth(get(channels), outsider)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/v1/channels/" + channelId + "/messages"), outsider)).andExpect(status().isForbidden());

        // El permiso se puede dar con un rol: con él, el miembro ya puede crear canales
        long memberProfile = ((Number) JsonPath.read(mvc.perform(auth(get("/api/v1/profiles/me/community/" + communityId), member))
                .andReturn().getResponse().getContentAsString(), "$.id")).longValue();
        long roleId = ((Number) JsonPath.read(mvc.perform(auth(post("/api/v1/roles"), owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Admin del chat\",\"communityId\":" + communityId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id")).longValue();
        mvc.perform(auth(post("/api/v1/role-permissions"), owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleId\":" + roleId + ",\"permissionId\":" + permissionId("MANAGE_CHANNELS") + "}"))
                .andExpect(status().isCreated());
        mvc.perform(auth(post("/api/v1/profiles/" + memberProfile + "/roles/" + roleId), owner))
                .andExpect(status().isOk());
        mvc.perform(auth(post(channels), member).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"off-topic\"}"))
                .andExpect(status().isCreated());

        mvc.perform(auth(delete("/api/v1/channels/" + channelId), owner)).andExpect(status().isNoContent());
        mvc.perform(auth(get(channels), member))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("off-topic"));
    }

    @Test
    void chatChannelsCountUnreadMessagesUntilTheyAreRead() throws Exception {
        String ownerEmail = uniqueEmail();
        String owner = register("Owner", ownerEmail);
        String member = register("Member", uniqueEmail());
        long communityId = createCommunity(owner, "No leídos " + UUID.randomUUID());
        joinCommunity(member, communityId);
        String channels = "/api/v1/communities/" + communityId + "/channels";
        long channelId = ((Number) JsonPath.read(mvc.perform(auth(post(channels), owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"general\"}"))
                .andReturn().getResponse().getContentAsString(), "$.id")).longValue();

        var ownerUser = userRepository.findByEmail(ownerEmail).orElseThrow();
        chatService.send(channelId, "Hola", ownerUser);
        chatService.send(channelId, "¿Hay alguien?", ownerUser);

        // El miembro tiene 2 nuevos; los propios no cuentan para quien los escribe
        mvc.perform(auth(get(channels), member)).andExpect(jsonPath("$[0].unreadCount").value(2));
        mvc.perform(auth(get(channels), owner)).andExpect(jsonPath("$[0].unreadCount").value(0));

        // Al leer el canal se ponen a cero, y vuelven a contar los que lleguen después
        mvc.perform(auth(put("/api/v1/channels/" + channelId + "/read"), member)).andExpect(status().isNoContent());
        mvc.perform(auth(get(channels), member)).andExpect(jsonPath("$[0].unreadCount").value(0));
        chatService.send(channelId, "Otro más", ownerUser);
        mvc.perform(auth(get(channels), member)).andExpect(jsonPath("$[0].unreadCount").value(1));
    }

    @Test
    void myPermissionsReflectOwnershipAndRole() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String member = register("Member", uniqueEmail());
        long communityId = createCommunity(owner, "Permisos");
        joinCommunity(member, communityId);

        mvc.perform(auth(get("/api/v1/communities/" + communityId + "/my-permissions"), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItems("MANAGE_COMMUNITY_SETTINGS", "BAN_USER", "ASSIGN_ROLES")));
        mvc.perform(auth(get("/api/v1/communities/" + communityId + "/my-permissions"), member))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }

    @Test
    void communityNamesAreUnique() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String name = "Única " + UUID.randomUUID();
        createCommunity(owner, name);

        mvc.perform(auth(multipart("/api/v1/communities/create")
                        .file(json("community", "{\"name\":\"" + name.toUpperCase() + "\"}")), owner))
                .andExpect(status().isConflict());
    }

    @Test
    void postListsArePaginatedNewestFirst() throws Exception {
        String owner = register("Owner", uniqueEmail());
        long communityId = createCommunity(owner, "Paginación " + UUID.randomUUID());
        for (int i = 1; i <= 3; i++) {
            createPost(owner, communityId, "Post " + i, "<p>x</p>");
        }

        mvc.perform(auth(get("/api/v1/posts/community/" + communityId).param("size", "2"), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Post 3"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.last").value(false));
        mvc.perform(auth(get("/api/v1/posts/community/" + communityId).param("size", "2").param("page", "1"), owner))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void cannotJoinTwice() throws Exception {
        String owner = register("Owner", uniqueEmail());
        long communityId = createCommunity(owner, "Cine");
        mvc.perform(auth(post("/api/v1/profiles/auto-create/community/" + communityId), owner))
                .andExpect(status().isConflict());
    }

    @Test
    void searchFindsCommunitiesByNameAndTag() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String name = "Astronomía " + UUID.randomUUID();
        createCommunity(owner, name);

        mvc.perform(get("/api/v1/communities/search").param("name", name.substring(0, 15)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem(name)));
        mvc.perform(get("/api/v1/communities/search").param("tag", "TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem(name)));
    }

    // ------------------------------------------------------------------ Publicaciones

    @Test
    void onlyAuthorOrModeratorsCanEditAndDeletePosts() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String author = register("Author", uniqueEmail());
        String other = register("Other", uniqueEmail());
        long communityId = createCommunity(owner, "Libros");
        joinCommunity(author, communityId);
        joinCommunity(other, communityId);

        long postId = createPost(author, communityId, "Mi post", "<p>Hola</p>");

        String update = "{\"title\":\"Editado\",\"content\":\"<p>x</p>\",\"tags\":[]}";
        mvc.perform(auth(put("/api/v1/posts/" + postId), other).contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isForbidden());
        mvc.perform(auth(delete("/api/v1/posts/" + postId), other))
                .andExpect(status().isForbidden());

        mvc.perform(auth(put("/api/v1/posts/" + postId), author).contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Editado"));

        // El creador de la comunidad tiene DELETE_POST
        mvc.perform(auth(delete("/api/v1/posts/" + postId), owner))
                .andExpect(status().isNoContent());
    }

    @Test
    void nonMembersCannotPost() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String outsider = register("Outsider", uniqueEmail());
        long communityId = createCommunity(owner, "Música");

        mvc.perform(auth(post("/api/v1/posts"), outsider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"content\":\"y\",\"communityId\":" + communityId + "}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postHtmlIsSanitized() throws Exception {
        String owner = register("Owner", uniqueEmail());
        long communityId = createCommunity(owner, "Seguridad");

        long postId = createPost(owner, communityId, "XSS",
                "<p class=\"ql-align-center\">Hola <strong>mundo</strong></p><script>alert(1)</script><img src=x onerror=alert(1)>");

        mvc.perform(auth(get("/api/v1/posts/" + postId), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", containsString("<strong>mundo</strong>")))
                .andExpect(jsonPath("$.content", containsString("ql-align-center")))
                .andExpect(jsonPath("$.content", not(containsString("<script"))))
                .andExpect(jsonPath("$.content", not(containsString("onerror"))));
    }

    @Test
    void postListIncludesLikeAndCommentCounters() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String member = register("Member", uniqueEmail());
        long communityId = createCommunity(owner, "Contadores");
        long memberProfileId = joinCommunity(member, communityId);
        long postId = createPost(owner, communityId, "Post", "<p>x</p>");

        mvc.perform(auth(post("/api/v1/likes/" + postId + "/" + memberProfileId), member)).andExpect(status().isOk());
        comment(member, postId, "¡Genial!");

        mvc.perform(auth(get("/api/v1/posts/community/" + communityId), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].likes").value(1))
                .andExpect(jsonPath("$.content[0].comments").value(1));
    }

    // ------------------------------------------------------------------ Likes, comentarios y notificaciones

    @Test
    void cannotLikeOnBehalfOfAnotherProfile() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String member = register("Member", uniqueEmail());
        long communityId = createCommunity(owner, "Likes");
        long memberProfileId = joinCommunity(member, communityId);
        long postId = createPost(owner, communityId, "Post", "<p>x</p>");

        mvc.perform(auth(post("/api/v1/likes/" + postId + "/" + memberProfileId), owner))
                .andExpect(status().isForbidden());
    }

    @Test
    void commentsGenerateNotificationsVisibleOnlyToTheirReceiver() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String member = register("Member", uniqueEmail());
        long communityId = createCommunity(owner, "Notificaciones");
        joinCommunity(member, communityId);
        long ownerProfileId = myProfileId(owner, communityId);
        long postId = createPost(owner, communityId, "Post", "<p>x</p>");

        long commentId = comment(member, postId, "Primer comentario");

        mvc.perform(auth(get("/api/v1/notifications/profile/" + ownerProfileId), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("COMMENT_POST"))
                .andExpect(jsonPath("$[0].sender.username").value("Member"));
        mvc.perform(auth(get("/api/v1/notifications/profile/" + ownerProfileId), member))
                .andExpect(status().isForbidden());

        // El autor del post no puede editar el comentario ajeno... salvo que tenga EDIT_COMMENTS (el creador sí)
        String newMember = register("Third", uniqueEmail());
        joinCommunity(newMember, communityId);
        mvc.perform(auth(put("/api/v1/comments/" + commentId), newMember)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"editado\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(auth(put("/api/v1/comments/" + commentId), owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"moderado\"}"))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------------ Roles

    @Test
    void moderatorsCannotEscalateTheirOwnPrivileges() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String mod = register("Mod", uniqueEmail());
        long communityId = createCommunity(owner, "Roles");
        long modProfileId = joinCommunity(mod, communityId);

        // El creador crea un rol "Moderador" con ASSIGN_ROLES y se lo asigna al moderador
        long modRoleId = ((Number) JsonPath.read(mvc.perform(auth(post("/api/v1/roles"), owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Moderador\",\"textColor\":\"#ffffff\",\"backgroundColor\":\"#123456\",\"communityId\":" + communityId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id")).longValue();
        long assignRolesPermissionId = permissionId("ASSIGN_ROLES");
        mvc.perform(auth(post("/api/v1/role-permissions"), owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleId\":" + modRoleId + ",\"permissionId\":" + assignRolesPermissionId + "}"))
                .andExpect(status().isCreated());
        mvc.perform(auth(post("/api/v1/profiles/" + modProfileId + "/roles/" + modRoleId), owner))
                .andExpect(status().isOk());

        // El moderador no puede asignarse el rol "Líder" ni añadir permisos a su propio rol
        long leaderRoleId = ((Number) JsonPath.read(mvc.perform(auth(get("/api/v1/profiles/me/community/" + communityId), owner))
                .andReturn().getResponse().getContentAsString(), "$.rol.id")).longValue();
        mvc.perform(auth(post("/api/v1/profiles/" + modProfileId + "/roles/" + leaderRoleId), mod))
                .andExpect(status().isForbidden());
        mvc.perform(auth(post("/api/v1/role-permissions"), mod)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleId\":" + modRoleId + ",\"permissionId\":" + permissionId("BAN_USER") + "}"))
                .andExpect(status().isForbidden());

        // Pero sí puede crear roles nuevos
        mvc.perform(auth(post("/api/v1/roles"), mod)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Novato\",\"textColor\":\"#000000\",\"backgroundColor\":\"#eeeeee\",\"communityId\":" + communityId + "}"))
                .andExpect(status().isCreated());
    }

    // ------------------------------------------------------------------ Cuenta

    @Test
    void deletingAccountRemovesOwnedCommunities() throws Exception {
        String owner = register("Owner", uniqueEmail());
        String member = register("Member", uniqueEmail());
        long communityId = createCommunity(owner, "Temporal");
        joinCommunity(member, communityId);
        createPost(member, communityId, "Post", "<p>x</p>");

        mvc.perform(auth(delete("/api/v1/user/me"), owner)).andExpect(status().isNoContent());

        mvc.perform(auth(get("/api/v1/communities/" + communityId), member)).andExpect(status().isNotFound());
        mvc.perform(auth(get("/api/v1/user/me"), owner)).andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------ Utilidades

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@test.com";
    }

    private static <T extends AbstractMockHttpServletRequestBuilder<T>> T auth(T request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    private static MockMultipartFile json(String name, String json) {
        return new MockMultipartFile(name, "", MediaType.APPLICATION_JSON_VALUE, json.getBytes(StandardCharsets.UTF_8));
    }

    private ResultActions registerRequest(String username, String email) throws Exception {
        return registerRequest(username, email, "password123");
    }

    private ResultActions registerRequest(String username, String email, String password) throws Exception {
        return mvc.perform(multipart("/api/v1/auth/register").file(json("user",
                "{\"username\":\"" + username + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")));
    }

    private String register(String username, String email) throws Exception {
        return JsonPath.read(registerRequest(username, email)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.token");
    }

    private long createCommunity(String token, String name) throws Exception {
        String body = mvc.perform(auth(multipart("/api/v1/communities/create")
                        .file(json("community", "{\"name\":\"" + name + "\",\"description\":\"<p>desc</p>\",\"tags\":[\"test\"]}")), token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long joinCommunity(String token, long communityId) throws Exception {
        String body = mvc.perform(auth(post("/api/v1/profiles/auto-create/community/" + communityId), token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long myProfileId(String token, long communityId) throws Exception {
        String body = mvc.perform(auth(get("/api/v1/profiles/me/community/" + communityId), token))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long createPost(String token, long communityId, String title, String content) throws Exception {
        String json = "{\"title\":\"" + title + "\",\"content\":\"" + content.replace("\"", "\\\"")
                + "\",\"tags\":[\"general\"],\"communityId\":" + communityId + "}";
        String body = mvc.perform(auth(post("/api/v1/posts"), token)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long comment(String token, long postId, String content) throws Exception {
        String body = mvc.perform(auth(post("/api/v1/comments"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postId\":" + postId + ",\"content\":\"" + content + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long permissionId(String name) throws Exception {
        String body = mvc.perform(get("/api/v1/permissions").header("Authorization", "Bearer " + register("P", uniqueEmail())))
                .andReturn().getResponse().getContentAsString();
        return ((Number) ((java.util.List<?>) JsonPath.read(body, "$[?(@.name == '" + name + "')].id")).getFirst()).longValue();
    }
}
