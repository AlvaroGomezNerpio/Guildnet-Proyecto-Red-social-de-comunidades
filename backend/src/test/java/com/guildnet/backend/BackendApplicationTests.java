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
