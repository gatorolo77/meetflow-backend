package com.meetflow.backend.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SignalingHandler extends TextWebSocketHandler {

    // Map roomCode -> (sessionId -> WebSocketSession)
    private final Map<String, Map<String, WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("📩 Signaling Message Received: " + payload);

        // Basic JSON routing without heavy library overhead
        if (payload.contains("\"type\":\"JOIN_ROOM\"")) {
            String roomCode = extractJsonValue(payload, "roomCode");
            rooms.computeIfAbsent(roomCode, k -> new ConcurrentHashMap<>()).put(session.getId(), session);
            System.out.println("👤 Peer " + session.getId() + " se unió a la sala " + roomCode);
            
            // Notify existing peers in room
            broadcastToRoom(roomCode, session.getId(), new TextMessage("{\"type\":\"USER_JOINED\",\"peerId\":\"" + session.getId() + "\"}"));
        } else if (payload.contains("\"type\":\"MEETING_ENDED\"")) {
            String roomCode = extractJsonValue(payload, "roomCode");
            System.out.println("🛑 Anfitrión finalizó la reunión en la sala: " + roomCode);
            if (roomCode != null) {
                broadcastToAllInRoom(roomCode, new TextMessage("{\"type\":\"MEETING_ENDED\",\"roomCode\":\"" + roomCode + "\"}"));
            }
        } else {
            // Broadcast SDP OFFER, ANSWER, CANDIDATE to all other peers in the room
            String roomCode = extractJsonValue(payload, "roomCode");
            if (roomCode != null && rooms.containsKey(roomCode)) {
                // Attach sender ID to payload safely at the last closing brace
                int lastBrace = payload.lastIndexOf('}');
                String updatedPayload = (lastBrace != -1)
                        ? payload.substring(0, lastBrace) + ",\"senderId\":\"" + session.getId() + "\"}"
                        : payload;
                broadcastToRoom(roomCode, session.getId(), new TextMessage(updatedPayload));
            }
        }
    }

    private void broadcastToRoom(String roomCode, String senderSessionId, TextMessage message) {
        Map<String, WebSocketSession> roomSessions = rooms.get(roomCode);
        if (roomSessions != null) {
            for (Map.Entry<String, WebSocketSession> entry : roomSessions.entrySet()) {
                if (!entry.getKey().equals(senderSessionId) && entry.getValue().isOpen()) {
                    try {
                        entry.getValue().sendMessage(message);
                    } catch (IOException e) {
                        System.err.println("Error enviando mensaje a peer: " + entry.getKey());
                    }
                }
            }
        }
    }

    private void broadcastToAllInRoom(String roomCode, TextMessage message) {
        Map<String, WebSocketSession> roomSessions = rooms.get(roomCode);
        if (roomSessions != null) {
            for (Map.Entry<String, WebSocketSession> entry : roomSessions.entrySet()) {
                if (entry.getValue().isOpen()) {
                    try {
                        entry.getValue().sendMessage(message);
                    } catch (IOException e) {
                        System.err.println("Error enviando mensaje a peer: " + entry.getKey());
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("❌ Peer desconectado: " + session.getId());
        rooms.forEach((roomCode, sessions) -> {
            if (sessions.remove(session.getId()) != null) {
                broadcastToRoom(roomCode, session.getId(), new TextMessage("{\"type\":\"USER_LEFT\",\"peerId\":\"" + session.getId() + "\"}"));
            }
        });
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return null;
        startIndex += searchKey.length();
        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex == -1) return null;
        return json.substring(startIndex, endIndex);
    }
}
