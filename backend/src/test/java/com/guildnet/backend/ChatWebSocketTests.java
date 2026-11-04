package com.guildnet.backend;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Chat en tiempo real de extremo a extremo: conexión STOMP con JWT, envío, recepción e historial. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChatWebSocketTests {

    @LocalServerPort
    private int port;

    @Test
    void membersReceiveMessagesInRealTimeAndHistoryIsSaved() throws Exception {
        RestClient rest = RestClient.create("http://localhost:" + port);
        String token = register(rest);
        long communityId = createCommunity(rest, token);
        long channelId = createChannel(rest, token, communityId, "general");

        StompSession session = connect(token);
        BlockingQueue<String> received = new LinkedBlockingQueue<>();
        session.subscribe("/topic/chat/channel/" + channelId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                received.add(new String((byte[]) payload, StandardCharsets.UTF_8));
            }
        });
        Thread.sleep(300); // da tiempo a que se registre la suscripción

        StompHeaders send = new StompHeaders();
        send.setDestination("/app/chat/channel/" + channelId);
        send.setContentType(MediaType.APPLICATION_JSON);
        session.send(send, "{\"content\":\"¡Hola a todos!\"}".getBytes(StandardCharsets.UTF_8));

        String message = received.poll(5, TimeUnit.SECONDS);
        assertThat(message).isNotNull();
        assertThat((String) JsonPath.read(message, "$.content")).isEqualTo("¡Hola a todos!");
        assertThat((String) JsonPath.read(message, "$.username")).isEqualTo("Chatter");
        assertThat(((Number) JsonPath.read(message, "$.channelId")).longValue()).isEqualTo(channelId);

        String history = rest.get().uri("/api/v1/channels/" + channelId + "/messages")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve().body(String.class);
        assertThat((String) JsonPath.read(history, "$[0].content")).isEqualTo("¡Hola a todos!");
        session.disconnect();
    }

    @Test
    void connectionWithoutValidTokenIsRejected() {
        assertThatThrownBy(() -> connect("token-falso")).isInstanceOf(ExecutionException.class);
    }

    // ------------------------------------------------------------------ Utilidades

    private StompSession connect(String token) throws Exception {
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        // Conversor de bytes que acepta JSON (el contenido se maneja como texto en el test)
        client.setMessageConverter(new ByteArrayMessageConverter() {
            {
                addSupportedMimeTypes(MimeTypeUtils.APPLICATION_JSON);
            }
        });
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        handshakeHeaders.setOrigin("http://localhost:4200");
        return client.connectAsync(URI.create("ws://localhost:" + port + "/ws"), handshakeHeaders, connectHeaders,
                        new StompSessionHandlerAdapter() {
                        })
                .get(5, TimeUnit.SECONDS);
    }

    private static String register(RestClient rest) {
        var parts = new LinkedMultiValueMap<String, Object>();
        parts.add("user", jsonPart("{\"username\":\"Chatter\",\"email\":\"chat-" + UUID.randomUUID()
                + "@test.com\",\"password\":\"password123\"}"));
        String body = rest.post().uri("/api/v1/auth/register")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts)
                .retrieve().body(String.class);
        return JsonPath.read(body, "$.token");
    }

    private static long createCommunity(RestClient rest, String token) {
        var parts = new LinkedMultiValueMap<String, Object>();
        parts.add("community", jsonPart("{\"name\":\"Chat test\",\"tags\":[]}"));
        String body = rest.method(HttpMethod.POST).uri("/api/v1/communities/create")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts)
                .retrieve().body(String.class);
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private static long createChannel(RestClient rest, String token, long communityId, String name) {
        String body = rest.post().uri("/api/v1/communities/" + communityId + "/channels")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"name\":\"" + name + "\"}")
                .retrieve().body(String.class);
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private static HttpEntity<String> jsonPart(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(json, headers);
    }
}
