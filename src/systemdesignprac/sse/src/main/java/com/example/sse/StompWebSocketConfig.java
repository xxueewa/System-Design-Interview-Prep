package com.example.sse;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over WebSocket configuration.
 *
 * STOMP (Simple Text Oriented Messaging Protocol) adds a messaging layer on top of
 * raw WebSocket. Instead of raw frames, clients publish/subscribe to named destinations
 * (like topics or queues), and Spring routes messages automatically.
 *
 * Endpoints:
 *   ws://localhost:8080/stomp         — raw WebSocket with STOMP
 *   http://localhost:8080/stomp       — SockJS fallback (for browsers behind proxies)
 *
 * Message flow:
 *   Client → /app/chat               → @MessageMapping("/chat") in StompChatController
 *   Server → /topic/messages         → broadcast to all subscribers
 *   Server → /queue/reply-{session}  → private reply to one client
 */
@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-memory broker handles subscriptions to /topic (broadcast) and /queue (point-to-point)
        // In production, replace with a full broker: registry.enableStompBrokerRelay(...)
        // pointed at RabbitMQ or ActiveMQ for scalability across multiple server instances
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages routed to @MessageMapping methods in controllers
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // enables SockJS fallback for browser clients
    }
}