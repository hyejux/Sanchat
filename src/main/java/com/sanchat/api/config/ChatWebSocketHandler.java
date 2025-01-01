package com.sanchat.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanchat.api.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j // logger 자동 생성
@Component // Bean 등록
@RequiredArgsConstructor // final 인 매개변수 포함한 생성자 자동 생성
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;

    // 현재 연결된 세션들
    private final Set<WebSocketSession> sessions = new HashSet<>();

    // 채팅방 번호로 세션을 Map에 저장
    private final Map<Long, Set<WebSocketSession>> chatRoomSessionMap = new HashMap<>();

    // 소켓 연결 후 연결 확인
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("websocket: {} 연결됨", session.getId());
        sessions.add(session);
        session.sendMessage(new TextMessage("채팅방 연결 완료"));
    }

    // 소켓 메세지 처리
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("payload: {}", payload);

        ChatMessageDTO chatMessageDTO = objectMapper.readValue(payload, ChatMessageDTO.class);
        log.info("chatMessageDTO: {}", chatMessageDTO);

        // 현재 Map 에 채팅방에 대한 세션이 없으면 새로 생성
        Long chatRoomNo = chatMessageDTO.getChatRoomNo();
        if(!chatRoomSessionMap.containsKey(chatRoomNo)) {
            chatRoomSessionMap.put(chatRoomNo, new HashSet<>());
        }
        Set<WebSocketSession> chatRoomSessions = chatRoomSessionMap.get(chatRoomNo);

        switch (chatMessageDTO.getMessageType()) {
            case ENTER:
                chatRoomSessions.add(session);
                chatRoomSessions.parallelStream().forEach(sess -> sendMessage(chatMessageDTO.getUserNo() + "님이 입장하였습니다.", sess));
                break;
            case CHAT:
                sendMessageToChatRoom(chatMessageDTO, chatRoomSessions);
                break;
            case EXIT:
                chatRoomSessions.remove(session);
                chatRoomSessions.parallelStream().forEach(sess -> sendMessage(chatMessageDTO.getUserNo() + "님이 나갔습니다.", sess));
                break;
            default:
                log.warn("unknown message type: {}", chatMessageDTO.getMessageType());
        }

    }

    // 채팅 관련 메소드
    private void sendMessageToChatRoom(ChatMessageDTO chatMessageDTO, Set<WebSocketSession> chatRoomSessions) {
        chatRoomSessions.parallelStream().forEach(session -> sendMessage(chatMessageDTO, session));
    }

    public <T> void sendMessage(T message, WebSocketSession session) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    // 소켓 연결 해제
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("websocket: {} 연결 해제", session.getId());
        sessions.remove(session);
        chatRoomSessionMap.values().forEach(roomSessions -> roomSessions.remove(session));
    }

}
