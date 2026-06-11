package com.hamplz.quizjam.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class UserIdHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
        ServerHttpRequest request,
        WebSocketHandler wsHandler,
        Map<String, Object> attributes
    ) {
        Object userId = attributes.get(JwtHandshakeInterceptor.ATTR_USER_ID);
        if (userId instanceof Long id) {
            return new StompPrincipal(String.valueOf(id));
        }
        return new StompPrincipal("anonymous-" + UUID.randomUUID());
    }

    private record StompPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
