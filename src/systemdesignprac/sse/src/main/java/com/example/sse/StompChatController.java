package com.example.sse;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Handles STOMP messages sent to /app/* destinations.
 *
 * Two messaging patterns demonstrated:
 *   1. Broadcast  — one message fan-out to all subscribers of a topic
 *   2. Private    — server sends a reply to a single client's queue
 */
@Controller
public class StompChatController {

    // Used for server-initiated pushes (e.g. triggered by Kafka events)
    private final SimpMessagingTemplate messagingTemplate;

    public StompChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Pattern 1 — Broadcast
     *
     * Client publishes to:  /app/chat
     * Server broadcasts to: /topic/messages  (all subscribers receive it)
     *
     * Example client message payload: { "sender": "Alice", "content": "Hello!" }
     */
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage handleChat(ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        System.out.println("[STOMP] Message from session " + sessionId + ": " + message.content());
        return new ChatMessage(message.sender(), message.content());
    }

    /**
     * Pattern 2 — Private reply
     *
     * Client publishes to:      /app/private
     * Server replies only to:   /queue/reply (scoped to the sender's session)
     *
     * SimpMessagingTemplate.convertAndSendToUser() automatically prepends /queue/reply
     * with the session ID, so only the sender receives it.
     */
    @MessageMapping("/private")
    public void handlePrivate(ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        System.out.println("[STOMP] Private message from session " + sessionId);

        ChatMessage reply = new ChatMessage("Server", "Private reply to: " + message.content());
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/reply", reply, headerAccessor.getMessageHeaders());
    }

    /**
     * Server-initiated push — call this from anywhere (e.g. a Kafka consumer)
     * to broadcast a notification to all STOMP subscribers.
     *
     * Example: stompChatController.pushNotification("UserA followed you");
     */
    public void pushNotification(String content) {
        messagingTemplate.convertAndSend("/topic/notifications",
                new ChatMessage("System", content));
    }

    /**
     * Simple record used as the STOMP message payload.
     * Spring automatically serializes/deserializes this to/from JSON.
     */
    public record ChatMessage(String sender, String content) {}
}