package com.pesocial.config;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public WebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode node = objectMapper.readTree(message.getPayload());
        String type = node.path("type").asText();

        Map<String, Object> outbound = switch (type) {
            case "DM" -> Map.of(
                "type", "DM",
                "senderId", node.path("senderId").asText(),
                "receiverId", node.path("receiverId").asText(),
                "message", node.path("message").asText(),
                "chatRoomId", node.path("chatRoomId").asText(),
                "messageId", node.path("messageId").asText(),
                "timestamp", Instant.now().toString());
            case "TYPING" -> Map.of(
                "type", "TYPING",
                "senderId", node.path("senderId").asText(),
                "receiverId", node.path("receiverId").asText(),
                "typing", node.path("typing").asBoolean(),
                "timestamp", Instant.now().toString());
            case "SEEN" -> Map.of(
                "type", "SEEN",
                "senderId", node.path("senderId").asText(),
                "receiverId", node.path("receiverId").asText(),
                "chatRoomId", node.path("chatRoomId").asText(),
                "messageId", node.path("messageId").asText(),
                "seen", true,
                "timestamp", Instant.now().toString());
            case "NOTIFICATION" -> Map.of(
                "type", "NOTIFICATION",
                "userId", node.path("userId").asText(),
                "message", node.path("message").asText(),
                "timestamp", Instant.now().toString());
            default -> Map.of(
                "type", "ERROR",
                "message", "Unsupported event type");
        };

        broadcast(outbound);
    }

    private void broadcast(Map<String, Object> payload) throws IOException {
        String json = objectMapper.writeValueAsString(payload);
        for (WebSocketSession webSocketSession : sessions) {
            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(new TextMessage(json));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }
}
