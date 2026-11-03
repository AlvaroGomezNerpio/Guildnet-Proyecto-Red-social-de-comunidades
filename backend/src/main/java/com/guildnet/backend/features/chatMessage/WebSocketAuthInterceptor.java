package com.guildnet.backend.features.chatMessage;

import com.guildnet.backend.features.user.User;
import com.guildnet.backend.features.user.UserRepository;
import com.guildnet.backend.security.AccessGuard;
import com.guildnet.backend.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Autentica la conexión STOMP con el JWT (cabecera {@code Authorization} del frame CONNECT)
 * y solo deja suscribirse al chat de una comunidad a sus miembros.
 */
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Pattern CHAT_TOPIC = Pattern.compile("^/topic/chat/(\\d+)$");

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AccessGuard accessGuard;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String header = accessor.getFirstNativeHeader("Authorization");
            String token = header != null && header.startsWith("Bearer ") ? header.substring(7) : null;

            User user = token == null ? null : jwtService.extractUserId(token).flatMap(userRepository::findById).orElse(null);
            if (user == null) {
                throw new MessageDeliveryException("Token no válido");
            }
            accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            User user = currentUser(accessor);
            String destination = accessor.getDestination();
            Matcher matcher = destination == null ? null : CHAT_TOPIC.matcher(destination);
            if (matcher == null || !matcher.matches()) {
                throw new MessageDeliveryException("Destino no permitido");
            }
            if (accessGuard.findMembership(user, Long.parseLong(matcher.group(1))).isEmpty()) {
                throw new MessageDeliveryException("No perteneces a esta comunidad");
            }
        }

        if (StompCommand.SEND.equals(accessor.getCommand())) {
            currentUser(accessor);
        }

        return message;
    }

    private static User currentUser(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth && auth.getPrincipal() instanceof User user) {
            return user;
        }
        throw new MessageDeliveryException("Conexión no autenticada");
    }
}
