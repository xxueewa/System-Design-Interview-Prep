package com.example.sse;

import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Enumeration;
import java.util.Map;

/**
 * Registers the WebSocket handler at the /ws endpoint.
 *
 * setAllowedOrigins("*") is permissive for local dev — restrict in production.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws")
                .addInterceptors(new HttpSessionHandshakeInterceptor(){
                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception ex) {
                        System.out.println("[INFO] after handshake: " + response.toString() + " Exception: " + ex);
                    }
                })
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected boolean isValidOrigin(@Nullable ServerHttpRequest request) {
                        return true; // bypass Tomcat-level origin check
                    }
                });
    }
}